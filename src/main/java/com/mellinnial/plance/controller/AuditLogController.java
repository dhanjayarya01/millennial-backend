package com.mellinnial.plance.controller;

import com.mellinnial.plance.dto.response.ApiResponseDto;
import com.mellinnial.plance.dto.response.AuditLogResponseDto;
import com.mellinnial.plance.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Audit Logs", description = "View system audit / activity logs (admin only)")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponseDto<List<AuditLogResponseDto>>> getAllLogs() {
        List<AuditLogResponseDto> response = auditLogService.getAllLogs();
        return ResponseEntity.ok(ApiResponseDto.success("Audit logs fetched successfully", response));
    }
}
