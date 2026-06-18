package com.mellinnial.plance.controller;

import com.mellinnial.plance.dto.response.ApiResponseDto;
import com.mellinnial.plance.dto.response.AuditLogResponseDto;
import com.mellinnial.plance.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponseDto<List<AuditLogResponseDto>>> getAllLogs() {
        List<AuditLogResponseDto> response = auditLogService.getAllLogs();
        return ResponseEntity.ok(ApiResponseDto.success("Audit logs fetched successfully", response));
    }
}
