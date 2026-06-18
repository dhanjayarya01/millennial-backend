package com.mellinnial.plance.controller;

import com.mellinnial.plance.dto.response.ApiResponseDto;
import com.mellinnial.plance.dto.response.ReportResponseDto;
import com.mellinnial.plance.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> getProjectReport(@PathVariable Long projectId) {
        ReportResponseDto response = reportService.getProjectReport(projectId);
        return ResponseEntity.ok(ApiResponseDto.success("Project report fetched successfully", response));
    }

    @GetMapping("/employees/{employeeId}")
    public ResponseEntity<ApiResponseDto<ReportResponseDto>> getEmployeeReport(@PathVariable Long employeeId) {
        ReportResponseDto response = reportService.getEmployeeReport(employeeId);
        return ResponseEntity.ok(ApiResponseDto.success("Employee report fetched successfully", response));
    }
}
