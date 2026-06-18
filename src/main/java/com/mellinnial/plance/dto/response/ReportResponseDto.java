package com.mellinnial.plance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private Double completionPercentage;
    private Long totalTasks;
    private Long completedTasks;
    private Long pendingTasks;
    private Double totalHoursLogged;
    private Double averageCompletionTimeHours;
    private List<TaskResponseDto> taskDetails;
}
