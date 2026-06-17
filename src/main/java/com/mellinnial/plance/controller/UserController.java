package com.mellinnial.plance.controller;

import com.mellinnial.plance.dto.response.ApiResponseDto;
import com.mellinnial.plance.dto.response.UserResponseDto;
import com.mellinnial.plance.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
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
}
