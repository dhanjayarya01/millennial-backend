package com.mellinnial.plance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String role;
    private boolean active;
    private boolean verified;
    private String profilePictureUrl;
    private LocalDateTime createdAt;
}
