package com.mellinnial.plance.controller;

import com.mellinnial.plance.dto.request.TaskRequestDto;
import com.mellinnial.plance.dto.request.TaskStatusUpdateDto;
import com.mellinnial.plance.dto.response.ApiResponseDto;
import com.mellinnial.plance.dto.response.TaskResponseDto;
import com.mellinnial.plance.service.TaskService;
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
@Tag(name = "Tasks", description = "Create, update, delete and manage tasks within projects")
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<ApiResponseDto<TaskResponseDto>> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody TaskRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TaskResponseDto response = taskService.createTask(projectId, dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Task created successfully", response));
    }

    @GetMapping("/tasks")
    public ResponseEntity<ApiResponseDto<List<TaskResponseDto>>> getAllTasks(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        List<TaskResponseDto> response = taskService.getAllTasks(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Tasks fetched successfully", response));
    }

    @GetMapping("/tasks/{id}")
    public ResponseEntity<ApiResponseDto<TaskResponseDto>> getTaskById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TaskResponseDto response = taskService.getTaskById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Task fetched successfully", response));
    }

    @PutMapping("/tasks/{id}")
    public ResponseEntity<ApiResponseDto<TaskResponseDto>> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TaskResponseDto response = taskService.updateTask(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Task updated successfully", response));
    }

    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<ApiResponseDto<TaskResponseDto>> updateTaskStatus(
            @PathVariable Long id,
            @Valid @RequestBody TaskStatusUpdateDto dto,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TaskResponseDto response = taskService.updateTaskStatus(id, dto, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Task status updated successfully", response));
    }

    @PutMapping("/tasks/{id}/assign/{employeeId}")
    public ResponseEntity<ApiResponseDto<TaskResponseDto>> assignTask(
            @PathVariable Long id,
            @PathVariable Long employeeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        TaskResponseDto response = taskService.assignTask(id, employeeId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Task assigned successfully", response));
    }

    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<ApiResponseDto<String>> deleteTask(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponseDto.success("Task deleted successfully"));
    }
}
