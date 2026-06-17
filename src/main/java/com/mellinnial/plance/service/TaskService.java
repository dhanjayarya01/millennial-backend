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
                .priority(mapTaskPriority(dto.getPriority()))
                .status(mapTaskStatus(dto.getStatus()))
                .deadline(dto.getDeadline())
                .estimatedHours(dto.getEstimatedHours())
                .project(project)
                .createdBy(currentUser)
                .build();

        if (dto.getEmployeeIds() != null && !dto.getEmployeeIds().isEmpty()) {
            java.util.List<UserEntity> assignees = new java.util.ArrayList<>();
            for (Long empId : dto.getEmployeeIds()) {
                UserEntity employee = userRepository.findById(empId)
                        .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
                if (employee.getRole() == null || employee.getRole().getName() != RoleType.ROLE_EMPLOYEE) {
                    throw new IllegalArgumentException("Assigned user is not an employee");
                }
                if (project.getAssignedEmployees() == null) {
                    project.setAssignedEmployees(new java.util.HashSet<>());
                }
                if (!project.getAssignedEmployees().contains(employee)) {
                    project.getAssignedEmployees().add(employee);
                }
                assignees.add(employee);
            }
            projectRepository.save(project);
            setEmployeesSafely(task, assignees);
        } else if (dto.getEmployeeId() != null) {
            UserEntity employee = userRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            if (employee.getRole() == null || employee.getRole().getName() != RoleType.ROLE_EMPLOYEE) {
                throw new IllegalArgumentException("Assigned user is not an employee");
            }
            if (project.getAssignedEmployees() == null) {
                project.setAssignedEmployees(new java.util.HashSet<>());
            }
            if (!project.getAssignedEmployees().contains(employee)) {
                project.getAssignedEmployees().add(employee);
                projectRepository.save(project);
            }
            setEmployeesSafely(task, java.util.List.of(employee));
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
            tasks = taskRepository.findByEmployeesContaining(currentUser);
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
        task.setPriority(mapTaskPriority(dto.getPriority()));
        task.setStatus(mapTaskStatus(dto.getStatus()));
        task.setDeadline(dto.getDeadline());
        task.setEstimatedHours(dto.getEstimatedHours());

        ProjectEntity project = task.getProject();
        if (dto.getEmployeeIds() != null && !dto.getEmployeeIds().isEmpty()) {
            java.util.List<UserEntity> assignees = new java.util.ArrayList<>();
            for (Long empId : dto.getEmployeeIds()) {
                UserEntity employee = userRepository.findById(empId)
                        .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
                if (employee.getRole() == null || employee.getRole().getName() != RoleType.ROLE_EMPLOYEE) {
                    throw new IllegalArgumentException("Assigned user is not an employee");
                }
                if (project.getAssignedEmployees() == null) {
                    project.setAssignedEmployees(new java.util.HashSet<>());
                }
                if (!project.getAssignedEmployees().contains(employee)) {
                    project.getAssignedEmployees().add(employee);
                }
                assignees.add(employee);
            }
            projectRepository.save(project);
            setEmployeesSafely(task, assignees);
        } else if (dto.getEmployeeId() != null) {
            UserEntity employee = userRepository.findById(dto.getEmployeeId())
                    .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
            if (employee.getRole() == null || employee.getRole().getName() != RoleType.ROLE_EMPLOYEE) {
                throw new IllegalArgumentException("Assigned user is not an employee");
            }
            if (project.getAssignedEmployees() == null) {
                project.setAssignedEmployees(new java.util.HashSet<>());
            }
            if (!project.getAssignedEmployees().contains(employee)) {
                project.getAssignedEmployees().add(employee);
                projectRepository.save(project);
            }
            setEmployeesSafely(task, java.util.List.of(employee));
        } else {
            setEmployeesSafely(task, null);
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
                || (role == RoleType.ROLE_EMPLOYEE && task.getEmployees() != null && task.getEmployees().stream().anyMatch(e -> e.getId().equals(currentUser.getId())));

        if (!isAuthorized) {
            throw new AccessDeniedException("You are not authorized to update this task status");
        }

        task.setStatus(mapTaskStatus(dto.getStatus()));
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

        if (employee.getRole() == null || employee.getRole().getName() != RoleType.ROLE_EMPLOYEE) {
            throw new IllegalArgumentException("User is not an employee");
        }

        if (task.getProject().getAssignedEmployees() == null) {
            task.getProject().setAssignedEmployees(new java.util.HashSet<>());
        }
        if (!task.getProject().getAssignedEmployees().contains(employee)) {
            task.getProject().getAssignedEmployees().add(employee);
            projectRepository.save(task.getProject());
        }

        setEmployeesSafely(task, java.util.List.of(employee));
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

    private void setEmployeesSafely(TaskEntity task, java.util.List<UserEntity> newEmployees) {
        if (task.getEmployees() == null) {
            task.setEmployees(new java.util.ArrayList<>());
        } else {
            task.getEmployees().clear();
        }
        if (newEmployees != null) {
            task.getEmployees().addAll(newEmployees);
        }
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

        if (task.getEmployees() == null || task.getEmployees().stream().noneMatch(e -> e.getId().equals(currentUser.getId()))) {
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
                .employees(task.getEmployees() != null ? task.getEmployees().stream().filter(java.util.Objects::nonNull).map(authService::mapToUserResponse).filter(java.util.Objects::nonNull).collect(Collectors.toList()) : java.util.List.of())
                .employee(task.getEmployees() != null && !task.getEmployees().isEmpty() && task.getEmployees().get(0) != null ? authService.mapToUserResponse(task.getEmployees().get(0)) : null)
                .createdBy(task.getCreatedBy() != null ? authService.mapToUserResponse(task.getCreatedBy()) : null)
                .build();
    }

    private TaskStatus mapTaskStatus(String statusStr) {
        if (statusStr == null) return TaskStatus.TO_DO;
        String s = statusStr.replace("-", "_").toUpperCase();
        if (s.equals("TODO") || s.equals("TO_DO")) return TaskStatus.TO_DO;
        if (s.equals("IN_PROGRESS")) return TaskStatus.IN_PROGRESS;
        if (s.equals("REVIEW") || s.equals("IN_REVIEW")) return TaskStatus.IN_REVIEW;
        if (s.equals("DONE") || s.equals("COMPLETED")) return TaskStatus.COMPLETED;
        if (s.equals("BLOCKED")) return TaskStatus.BLOCKED;
        return TaskStatus.TO_DO;
    }

    private TaskPriority mapTaskPriority(String priorityStr) {
        if (priorityStr == null) return TaskPriority.MEDIUM;
        String p = priorityStr.toUpperCase();
        if (p.equals("LOW")) return TaskPriority.LOW;
        if (p.equals("MEDIUM")) return TaskPriority.MEDIUM;
        if (p.equals("HIGH")) return TaskPriority.HIGH;
        if (p.equals("URGENT") || p.equals("CRITICAL")) return TaskPriority.CRITICAL;
        return TaskPriority.MEDIUM;
    }
}
