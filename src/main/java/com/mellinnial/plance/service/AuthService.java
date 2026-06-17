package com.mellinnial.plance.service;

import com.mellinnial.plance.dto.request.LoginRequestDto;
import com.mellinnial.plance.dto.request.RegisterRequestDto;
import com.mellinnial.plance.dto.response.AuthResponseDto;
import com.mellinnial.plance.dto.response.UserResponseDto;
import com.mellinnial.plance.entity.RoleEntity;
import com.mellinnial.plance.entity.UserEntity;
import com.mellinnial.plance.entity.enums.RoleType;
import com.mellinnial.plance.repository.RoleRepository;
import com.mellinnial.plance.repository.UserRepository;
import com.mellinnial.plance.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public UserResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + request.getEmail());
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + request.getUsername());
        }

        RoleType roleType;
        try {
            roleType = RoleType.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role. Must be one of: ROLE_ADMIN, ROLE_PROJECT_MANAGER, ROLE_EMPLOYEE");
        }

        RoleEntity role = roleRepository.findByName(roleType)
                .orElseThrow(() -> new IllegalStateException("Role not found in the database."));

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName())
                .username(request.getUsername().toLowerCase())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .verified(false)
                .build();

        UserEntity savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail().toLowerCase());
        
        UserEntity user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("User not found: " + request.getEmail()));

        String jwt = jwtService.generateToken(userDetails);

        return AuthResponseDto.builder()
                .accessToken(jwt)
                .user(mapToUserResponse(user))
                .build();
    }

    public UserResponseDto mapToUserResponse(UserEntity user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().getName().name())
                .active(user.isActive())
                .verified(user.isVerified())
                .profilePictureUrl(user.getProfilePictureUrl())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
