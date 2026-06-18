package com.mellinnial.plance.controller;

import com.mellinnial.plance.dto.response.ApiResponseDto;
import com.mellinnial.plance.dto.response.UserResponseDto;
import com.mellinnial.plance.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management – verification, profile picture, and password changes")
public class UserController {

    private final UserService userService;

    @PutMapping("/{id}/verify")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> verifyUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        UserResponseDto response = userService.verifyUser(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("User verified successfully", response));
    }

    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<ApiResponseDto<java.util.List<UserResponseDto>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponseDto.success("Users retrieved successfully", userService.getAllUsers()));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{id}/role")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateRole(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String role = body.get("role");
        UserResponseDto response = userService.updateRole(id, role, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("User role updated successfully", response));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        userService.deleteUser(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("User deleted successfully"));
    }

    @PutMapping("/profile-picture")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> updateProfilePicture(
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String profilePictureUrl = body.get("profilePictureUrl");
        UserResponseDto response = userService.updateProfilePicture(userDetails.getUsername(), profilePictureUrl);
        return ResponseEntity.ok(ApiResponseDto.success("Profile picture updated successfully", response));
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponseDto<String>> changePassword(
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String currentPassword = body.get("currentPassword");
        String newPassword = body.get("newPassword");
        if (currentPassword == null || currentPassword.isBlank() || newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error("Current and new passwords are required"));
        }
        try {
            userService.changePassword(userDetails.getUsername(), currentPassword, newPassword);
            return ResponseEntity.ok(ApiResponseDto.success("Password changed successfully", "OK"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponseDto.error(e.getMessage()));
        }
    }
}
