package com.mellinnial.plance.service;

import com.mellinnial.plance.dto.response.UserResponseDto;
import com.mellinnial.plance.entity.UserEntity;
import com.mellinnial.plance.entity.RoleEntity;
import com.mellinnial.plance.entity.enums.RoleType;
import com.mellinnial.plance.repository.UserRepository;
import com.mellinnial.plance.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthService authService;

    @Transactional
    public UserResponseDto verifyUser(Long userId, String verifierEmail) {
        UserEntity verifier = userRepository.findByEmail(verifierEmail)
                .orElseThrow(() -> new IllegalArgumentException("Verifier not found"));

        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User to verify not found"));

        RoleType verifierRole = verifier.getRole().getName();
        RoleType targetRole = targetUser.getRole().getName();

        if (targetRole == RoleType.ROLE_ADMIN || targetRole == RoleType.ROLE_PROJECT_MANAGER) {
            if (verifierRole != RoleType.ROLE_ADMIN) {
                throw new AccessDeniedException("Only administrators can verify admins and project managers");
            }
        } else if (targetRole == RoleType.ROLE_EMPLOYEE) {
            if (verifierRole != RoleType.ROLE_ADMIN && verifierRole != RoleType.ROLE_PROJECT_MANAGER) {
                throw new AccessDeniedException("Only admins and project managers can verify employees");
            }
        }

        targetUser.setVerified(true);
        UserEntity saved = userRepository.save(targetUser);
        return authService.mapToUserResponse(saved);
    }

    @Transactional(readOnly = true)
    public java.util.List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(authService::mapToUserResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public UserResponseDto updateRole(Long userId, String roleName, String updaterEmail) {
        UserEntity updater = userRepository.findByEmail(updaterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Updater not found"));
        if (updater.getRole().getName() != RoleType.ROLE_ADMIN) {
            throw new org.springframework.security.access.AccessDeniedException("Only administrators can update user roles");
        }
        UserEntity targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        RoleType roleType;
        try {
            roleType = RoleType.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role");
        }
        RoleEntity role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new IllegalStateException("Role not found"));
        targetUser.setRole(role);
        UserEntity saved = userRepository.save(targetUser);
        return authService.mapToUserResponse(saved);
    }

    @Transactional
    public void deleteUser(Long userId, String deleterEmail) {
        UserEntity deleter = userRepository.findByEmail(deleterEmail)
                .orElseThrow(() -> new IllegalArgumentException("Deleter not found"));
        RoleType deleterRole = deleter.getRole().getName();
        if (deleterRole != RoleType.ROLE_ADMIN && deleterRole != RoleType.ROLE_PROJECT_MANAGER) {
            throw new org.springframework.security.access.AccessDeniedException("Only admins and project managers can delete users");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public UserResponseDto updateProfilePicture(String email, String profilePictureUrl) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setProfilePictureUrl(profilePictureUrl);
        UserEntity saved = userRepository.save(user);
        return authService.mapToUserResponse(saved);
    }
}
