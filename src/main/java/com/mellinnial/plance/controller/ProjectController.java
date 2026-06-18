package com.mellinnial.plance.controller;

import com.mellinnial.plance.dto.request.ProjectRequestDto;
import com.mellinnial.plance.dto.response.ApiResponseDto;
import com.mellinnial.plance.dto.response.ProjectResponseDto;
import com.mellinnial.plance.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "CRUD operations and member management for projects")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<ProjectResponseDto>> createProject(
            @Valid @RequestBody ProjectRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProjectResponseDto response = projectService.createProject(dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Project created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<ProjectResponseDto>>> getAllProjects(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<ProjectResponseDto> response = projectService.getAllProjects(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Projects fetched successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProjectResponseDto>> getProjectById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProjectResponseDto response = projectService.getProjectById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Project fetched successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<ProjectResponseDto>> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProjectResponseDto response = projectService.updateProject(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Project updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        projectService.deleteProject(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Project deleted successfully"));
    }

    @PutMapping("/{id}/manager/{managerId}")
    public ResponseEntity<ApiResponseDto<ProjectResponseDto>> assignManager(
            @PathVariable Long id,
            @PathVariable Long managerId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProjectResponseDto response = projectService.assignManager(id, managerId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Project manager assigned successfully", response));
    }

    @DeleteMapping("/{id}/manager")
    public ResponseEntity<ApiResponseDto<ProjectResponseDto>> removeManager(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProjectResponseDto response = projectService.removeManager(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Project manager removed successfully", response));
    }

    @PostMapping("/{id}/employees/{employeeId}")
    public ResponseEntity<ApiResponseDto<ProjectResponseDto>> assignEmployee(
            @PathVariable Long id,
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProjectResponseDto response = projectService.assignEmployee(id, employeeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Employee assigned to project successfully", response));
    }

    @DeleteMapping("/{id}/employees/{employeeId}")
    public ResponseEntity<ApiResponseDto<ProjectResponseDto>> removeEmployee(
            @PathVariable Long id,
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ProjectResponseDto response = projectService.removeEmployee(id, employeeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Employee removed from project successfully", response));
    }

    @PostMapping("/{id}/notify-all")
    public ResponseEntity<ApiResponseDto<String>> notifyAll(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String title = body.get("title");
        String description = body.get("description");
        String urgency = body.get("urgency");
        projectService.notifyAllMembers(id, title, description, urgency, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Custom SSE notifications broadcasted successfully", "OK"));
    }

    @PostMapping("/{id}/email-all")
    public ResponseEntity<ApiResponseDto<String>> emailAll(
            @PathVariable Long id,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String subject = body.get("subject");
        String emailBody = body.get("body");
        projectService.emailAllMembers(id, subject, emailBody, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Custom emails broadcasted successfully", "OK"));
    }
}
