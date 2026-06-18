package com.mellinnial.plance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WorkLogReplyRequestDto {

    @NotBlank(message = "Reply message is required")
    private String message;
}
