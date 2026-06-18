package com.mellinnial.plance.controller;

import com.mellinnial.plance.dto.request.WorkLogReplyRequestDto;
import com.mellinnial.plance.dto.request.WorkLogRequestDto;
import com.mellinnial.plance.dto.response.ApiResponseDto;
import com.mellinnial.plance.dto.response.WorkLogReplyResponseDto;
import com.mellinnial.plance.dto.response.WorkLogResponseDto;
import com.mellinnial.plance.service.WorkLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Work Logs", description = "Log work hours and replies on tasks")
public class WorkLogController {

    private final WorkLogService workLogService;

    @PostMapping("/work-logs")
    public ResponseEntity<ApiResponseDto<WorkLogResponseDto>> createWorkLog(
            @Valid @RequestBody WorkLogRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        WorkLogResponseDto response = workLogService.createWorkLog(dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Work log created successfully", response));
    }

    @PostMapping("/work-logs/{id}/replies")
    public ResponseEntity<ApiResponseDto<WorkLogReplyResponseDto>> createReply(
            @PathVariable Long id,
            @Valid @RequestBody WorkLogReplyRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        WorkLogReplyResponseDto response = workLogService.createReply(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Reply posted successfully", response));
    }

    @GetMapping("/work-logs")
    public ResponseEntity<ApiResponseDto<List<WorkLogResponseDto>>> getWorkLogs(
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<WorkLogResponseDto> response = workLogService.getWorkLogs(projectId, employeeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Work logs fetched successfully", response));
    }
}
