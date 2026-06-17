package com.mellinnial.plance.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TaskStatusUpdateDto {

    @NotBlank(message = "Status is required")
    private String status;
}
