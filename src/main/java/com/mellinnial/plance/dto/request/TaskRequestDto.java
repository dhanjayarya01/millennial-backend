package com.mellinnial.plance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskRequestDto {

    @NotBlank(message = "Task name is required")
    private String name;

    private String description;

    @NotBlank(message = "Priority is required")
    private String priority;

    @NotBlank(message = "Status is required")
    private String status;

    @NotNull(message = "Deadline is required")
    private LocalDate deadline;

    private Double estimatedHours;

    private Long employeeId;
    private java.util.List<Long> employeeIds;
}
