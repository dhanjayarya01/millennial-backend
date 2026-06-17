package com.mellinnial.plance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDto {
    private Long id;
    private String name;
    private String description;
    private String priority;
    private String status;
    private LocalDate deadline;
    private Double estimatedHours;
    private Long projectId;
    private String projectName;
    private UserResponseDto employee;
    private java.util.List<UserResponseDto> employees;
    private UserResponseDto createdBy;
}
