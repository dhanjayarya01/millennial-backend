package com.mellinnial.plance.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkLogRequestDto {

    @NotNull(message = "Task ID is required")
    private Long taskId;

    @NotBlank(message = "Log message is required")
    private String message;

    @NotNull(message = "Hours worked is required")
    @Min(value = 0, message = "Hours must be greater than zero")
    private Double hours;

    private String attachmentUrl;
}
