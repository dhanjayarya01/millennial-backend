package com.mellinnial.plance.service;

import com.mellinnial.plance.dto.request.TaskRequestDto;
import com.mellinnial.plance.dto.request.TaskStatusUpdateDto;
import com.mellinnial.plance.dto.response.TaskResponseDto;
import com.mellinnial.plance.entity.*;
import com.mellinnial.plance.entity.enums.RoleType;
import com.mellinnial.plance.repository.ProjectRepository;
import com.mellinnial.plance.repository.TaskRepository;
import com.mellinnial.plance.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectService projectService;
    private final AuthService authService;

    @Transactional
    public TaskResponseDto createTask(Long projectId, TaskRequestDto dto, String username) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        projectService.validateProjectManagementAccess(project, username);
        UserEntity currentUser = getCurrentUser(username);

        TaskEntity task = TaskEntity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .priority(TaskPriority.valueOf(dto.getPriority().toUpperCase()))
                .status(TaskStatus.valueOf(dto.getStatus().toUpperCase()))
                .deadline(dto.getDeadline())
                .estimatedHours(dto.getEstimatedHours())
                .project(project)
                .createdBy(currentUser)
                .build();

        if (dto.getEmployeeId() != null) {
            UserEntity employee = userRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            if (employee.getRole().getName() != RoleType.ROLE_EMPLOYEE) {
                throw new IllegalArgumentException("Assigned user is not an employee");
            }
            if (!project.getAssignedEmployees().contains(employee)) {
                throw new IllegalArgumentException("Employee must be assigned to the project first");
            }
            task.setEmployee(employee);
        }

        TaskEntity saved = taskRepository.save(task);
        return mapToTaskResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getAllTasks(String username) {
        UserEntity currentUser = getCurrentUser(username);
        RoleType role = currentUser.getRole().getName();

        List<TaskEntity> tasks;
        if (role == RoleType.ROLE_ADMIN) {
            tasks = taskRepository.findAll();
        } else if (role == RoleType.ROLE_PROJECT_MANAGER) {
            List<ProjectEntity> managedProjects = projectRepository.findByManager(currentUser);
            tasks = taskRepository.findByProjectIn(managedProjects);
        } else {
            tasks = taskRepository.findByEmployee(currentUser);
        }

        return tasks.stream()
                .map(this::mapToTaskResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id, String username) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        validateTaskAccess(task, username);
        return mapToTaskResponse(task);
    }

    @Transactional
    public TaskResponseDto updateTask(Long id, TaskRequestDto dto, String username) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        projectService.validateProjectManagementAccess(task.getProject(), username);

        task.setName(dto.getName());
        task.setDescription(dto.getDescription());
        task.setPriority(TaskPriority.valueOf(dto.getPriority().toUpperCase()));
        task.setStatus(TaskStatus.valueOf(dto.getStatus().toUpperCase()));
        task.setDeadline(dto.getDeadline());
        task.setEstimatedHours(dto.getEstimatedHours());

        if (dto.getEmployeeId() != null) {
            UserEntity employee = userRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            if (employee.getRole().getName() != RoleType.ROLE_EMPLOYEE) {
                throw new IllegalArgumentException("Assigned user is not an employee");
            }
            if (!task.getProject().getAssignedEmployees().contains(employee)) {
                throw new IllegalArgumentException("Employee must be assigned to the project first");
            }
            task.setEmployee(employee);
        } else {
            task.setEmployee(null);
        }

        TaskEntity saved = taskRepository.save(task);
        return mapToTaskResponse(saved);
    }

    @Transactional
    public TaskResponseDto updateTaskStatus(Long id, TaskStatusUpdateDto dto, String username) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        UserEntity currentUser = getCurrentUser(username);
        RoleType role = currentUser.getRole().getName();

        boolean isAuthorized = role == RoleType.ROLE_ADMIN
                || (role == RoleType.ROLE_PROJECT_MANAGER && task.getProject().getManager() != null && task.getProject().getManager().getId().equals(currentUser.getId()))
                || (role == RoleType.ROLE_EMPLOYEE && task.getEmployee() != null && task.getEmployee().getId().equals(currentUser.getId()));

        if (!isAuthorized) {
            throw new AccessDeniedException("You are not authorized to update this task status");
        }

        task.setStatus(TaskStatus.valueOf(dto.getStatus().toUpperCase()));
        TaskEntity saved = taskRepository.save(task);
        return mapToTaskResponse(saved);
    }

    @Transactional
    public TaskResponseDto assignTask(Long id, Long employeeId, String username) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        projectService.validateProjectManagementAccess(task.getProject(), username);

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (employee.getRole().getName() != RoleType.ROLE_EMPLOYEE) {
            throw new IllegalArgumentException("User is not an employee");
        }

        if (!task.getProject().getAssignedEmployees().contains(employee)) {
            throw new IllegalArgumentException("Employee must be assigned to the project first");
        }

        task.setEmployee(employee);
        TaskEntity saved = taskRepository.save(task);
        return mapToTaskResponse(saved);
    }

    @Transactional
    public void deleteTask(Long id, String username) {
        TaskEntity task = taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));
        projectService.validateProjectManagementAccess(task.getProject(), username);
        taskRepository.delete(task);
    }

    private void validateTaskAccess(TaskEntity task, String username) {
        UserEntity currentUser = getCurrentUser(username);
        RoleType role = currentUser.getRole().getName();

        if (role == RoleType.ROLE_ADMIN) {
            return;
        }

        if (role == RoleType.ROLE_PROJECT_MANAGER) {
            if (task.getProject().getManager() == null || !task.getProject().getManager().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Access denied to this task");
            }
            return;
        }

        if (task.getEmployee() == null || !task.getEmployee().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Access denied to this task");
        }
    }

    private UserEntity getCurrentUser(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));
    }

    private TaskResponseDto mapToTaskResponse(TaskEntity task) {
        return TaskResponseDto.builder()
                .id(task.getId())
                .name(task.getName())
                .description(task.getDescription())
                .priority(task.getPriority().name())
                .status(task.getStatus().name())
                .deadline(task.getDeadline())
                .estimatedHours(task.getEstimatedHours())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .employee(task.getEmployee() != null ? authService.mapToUserResponse(task.getEmployee()) : null)
                .createdBy(authService.mapToUserResponse(task.getCreatedBy()))
                .build();
    }
}
