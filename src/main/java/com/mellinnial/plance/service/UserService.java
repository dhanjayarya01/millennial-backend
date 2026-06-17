package com.mellinnial.plance.service;

import com.mellinnial.plance.dto.response.UserResponseDto;
import com.mellinnial.plance.entity.UserEntity;
import com.mellinnial.plance.entity.enums.RoleType;
import com.mellinnial.plance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
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
}
