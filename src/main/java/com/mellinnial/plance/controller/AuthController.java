package com.mellinnial.plance.controller;

import com.mellinnial.plance.dto.request.LoginRequestDto;
import com.mellinnial.plance.dto.request.RegisterRequestDto;
import com.mellinnial.plance.dto.response.ApiResponseDto;
import com.mellinnial.plance.dto.response.AuthResponseDto;
import com.mellinnial.plance.dto.response.UserResponseDto;
import com.mellinnial.plance.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints – no JWT required")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new account. Admin/Manager verification is required before login.")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> register(@Valid @RequestBody RegisterRequestDto request) {
        UserResponseDto response = authService.register(request);
        String message;
        if ("ROLE_ADMIN".equalsIgnoreCase(response.getRole()) || "ROLE_PROJECT_MANAGER".equalsIgnoreCase(response.getRole())) {
            message = "User registered successfully. Admin verification is required before you can log in.";
        } else {
            message = "User registered successfully. Project manager or admin verification is required before you can log in.";
        }
        return ResponseEntity.ok(ApiResponseDto.success(message, response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates the user and returns a JWT Bearer token.")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> login(@Valid @RequestBody LoginRequestDto request) {
        AuthResponseDto response = authService.login(request);
        return ResponseEntity.ok(ApiResponseDto.success("Authentication successful", response));
    }
}
