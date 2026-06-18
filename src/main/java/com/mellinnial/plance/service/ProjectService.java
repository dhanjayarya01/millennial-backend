package com.mellinnial.plance.service;

import com.mellinnial.plance.dto.request.ProjectRequestDto;
import com.mellinnial.plance.dto.response.ProjectResponseDto;
import com.mellinnial.plance.dto.response.UserResponseDto;
import com.mellinnial.plance.entity.ProjectEntity;
import com.mellinnial.plance.entity.ProjectStatus;
import com.mellinnial.plance.entity.UserEntity;
import com.mellinnial.plance.entity.enums.RoleType;
import com.mellinnial.plance.repository.ProjectRepository;
import com.mellinnial.plance.repository.TaskRepository;
import com.mellinnial.plance.repository.UserRepository;
import com.mellinnial.plance.repository.WorkLogRepository;
import com.mellinnial.plance.repository.NotificationRepository;
import com.mellinnial.plance.entity.TaskEntity;
import com.mellinnial.plance.entity.NotificationEntity;
import com.mellinnial.plance.entity.WorkLogEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final AuthService authService;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;
    private final WorkLogRepository workLogRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public ProjectResponseDto createProject(ProjectRequestDto dto, String username) {
        UserEntity currentUser = getCurrentUser(username);
        if (currentUser.getRole().getName() != RoleType.ROLE_ADMIN) {
            throw new AccessDeniedException("Only administrators can create projects");
        }

        ProjectEntity project = ProjectEntity.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .status(ProjectStatus.valueOf(dto.getStatus().replace("-", "_").toUpperCase()))
                .build();

        if (dto.getManagerId() != null) {
            UserEntity manager = userRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new IllegalArgumentException("Project Manager not found"));
            if (manager.getRole().getName() != RoleType.ROLE_PROJECT_MANAGER) {
                throw new IllegalArgumentException("User is not a Project Manager");
            }
            project.setManager(manager);
        }

        ProjectEntity saved = projectRepository.save(project);
        auditLogService.logAction(currentUser, "created", "Project", saved.getName(), "—", saved.getStatus().name(), saved.getId());
        
        // Notify manager if assigned on creation
        if (saved.getManager() != null) {
            notificationService.sendSseNotification(saved.getManager().getId(), null, "PROJECT_ASSIGNED", "Project Manager Assignment", "You are assigned as Manager for project: " + saved.getName());
            String emailHtml = "<p>Hello " + saved.getManager().getFullName() + ",</p><p>You are assigned as Manager for project: <b>" + saved.getName() + "</b>.</p>";
            notificationService.sendEmail(saved.getManager().getEmail(), "Project Manager Assignment", emailHtml);
        }
        
        return mapToProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDto> getAllProjects(String username) {
        UserEntity currentUser = getCurrentUser(username);
        RoleType role = currentUser.getRole().getName();

        List<ProjectEntity> projects;
        if (role == RoleType.ROLE_ADMIN) {
            projects = projectRepository.findAll();
        } else if (role == RoleType.ROLE_PROJECT_MANAGER) {
            projects = projectRepository.findByManager(currentUser);
        } else {
            projects = projectRepository.findByAssignedEmployeesContains(currentUser);
        }

        return projects.stream()
                .map(this::mapToProjectResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponseDto getProjectById(Long id, String username) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        validateProjectAccess(project, username);
        return mapToProjectResponse(project);
    }

    @Transactional
    public ProjectResponseDto updateProject(Long id, ProjectRequestDto dto, String username) {
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        UserEntity currentUser = getCurrentUser(username);
        RoleType role = currentUser.getRole().getName();

        if (role == RoleType.ROLE_ADMIN) {
            project.setName(dto.getName());
            project.setDescription(dto.getDescription());
            project.setStartDate(dto.getStartDate());
            project.setEndDate(dto.getEndDate());
            project.setStatus(ProjectStatus.valueOf(dto.getStatus().replace("-", "_").toUpperCase()));

            if (dto.getManagerId() != null) {
                UserEntity manager = userRepository.findById(dto.getManagerId())
                        .orElseThrow(() -> new IllegalArgumentException("Project Manager not found"));
                if (manager.getRole().getName() != RoleType.ROLE_PROJECT_MANAGER) {
                    throw new IllegalArgumentException("User is not a Project Manager");
                }
                project.setManager(manager);
            } else {
                project.setManager(null);
            }
        } else if (role == RoleType.ROLE_PROJECT_MANAGER) {
            if (project.getManager() == null || !project.getManager().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not the manager of this project");
            }
            project.setName(dto.getName());
            project.setDescription(dto.getDescription());
            project.setStartDate(dto.getStartDate());
            project.setEndDate(dto.getEndDate());
            project.setStatus(ProjectStatus.valueOf(dto.getStatus().replace("-", "_").toUpperCase()));
        } else {
            throw new AccessDeniedException("Employees cannot update projects");
        }

        ProjectEntity saved = projectRepository.save(project);
        auditLogService.logAction(currentUser, "updated", "Project", saved.getName(), "—", saved.getStatus().name(), saved.getId());
        return mapToProjectResponse(saved);
    }

    @Transactional
    public void deleteProject(Long id, String username) {
        UserEntity currentUser = getCurrentUser(username);
        if (currentUser.getRole().getName() != RoleType.ROLE_ADMIN) {
            throw new AccessDeniedException("Only administrators can delete projects");
        }
        ProjectEntity project = projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        List<TaskEntity> tasks = taskRepository.findByProject(project);
        if (!tasks.isEmpty()) {
            List<Long> taskIds = tasks.stream().map(TaskEntity::getId).collect(Collectors.toList());

            // Delete notifications
            List<NotificationEntity> notifications = notificationRepository.findByTaskIdIn(taskIds);
            if (!notifications.isEmpty()) {
                notificationRepository.deleteAll(notifications);
            }

            // Delete work logs (also deletes replies via CascadeType.ALL)
            List<WorkLogEntity> workLogs = workLogRepository.findByTaskIdIn(taskIds);
            if (!workLogs.isEmpty()) {
                workLogRepository.deleteAll(workLogs);
            }

            // Delete tasks
            taskRepository.deleteAll(tasks);
        }

        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponseDto assignManager(Long projectId, Long managerId, String username) {
        UserEntity currentUser = getCurrentUser(username);
        if (currentUser.getRole().getName() != RoleType.ROLE_ADMIN) {
            throw new AccessDeniedException("Only administrators can assign managers");
        }

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        UserEntity manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Project Manager not found"));

        if (manager.getRole().getName() != RoleType.ROLE_PROJECT_MANAGER) {
            throw new IllegalArgumentException("User is not a Project Manager");
        }

        project.setManager(manager);
        ProjectEntity saved = projectRepository.save(project);
        auditLogService.logAction(currentUser, "assigned_manager", "Project", saved.getName(), "—", manager.getFullName(), saved.getId());
        
        // Notify assigned manager
        notificationService.sendSseNotification(manager.getId(), null, "PROJECT_ASSIGNED", "Project Manager Assignment", "You are assigned as Manager for project: " + saved.getName());
        String emailHtml = "<p>Hello " + manager.getFullName() + ",</p><p>You are assigned as Manager for project: <b>" + saved.getName() + "</b>.</p>";
        notificationService.sendEmail(manager.getEmail(), "Project Manager Assignment", emailHtml);
        
        return mapToProjectResponse(saved);
    }

    @Transactional
    public ProjectResponseDto removeManager(Long projectId, String username) {
        UserEntity currentUser = getCurrentUser(username);
        if (currentUser.getRole().getName() != RoleType.ROLE_ADMIN) {
            throw new AccessDeniedException("Only administrators can remove managers");
        }

        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        project.setManager(null);
        ProjectEntity saved = projectRepository.save(project);
        return mapToProjectResponse(saved);
    }

    @Transactional
    public ProjectResponseDto assignEmployee(Long projectId, Long employeeId, String username) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        validateProjectManagementAccess(project, username);

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (employee.getRole().getName() != RoleType.ROLE_EMPLOYEE) {
            throw new IllegalArgumentException("User is not an Employee");
        }

        project.getAssignedEmployees().add(employee);
        ProjectEntity saved = projectRepository.save(project);
        UserEntity currentUser = getCurrentUser(username);
        auditLogService.logAction(currentUser, "assigned_employee", "Project", saved.getName(), "—", employee.getFullName(), saved.getId());
        
        // Notify assigned employee
        notificationService.sendSseNotification(employee.getId(), null, "PROJECT_ASSIGNED", "Project Assignment", "You have been assigned to project: " + saved.getName());
        String emailHtml = "<p>Hello " + employee.getFullName() + ",</p><p>You have been assigned to the project: <b>" + saved.getName() + "</b>.</p>";
        notificationService.sendEmail(employee.getEmail(), "Project Assignment", emailHtml);
        
        return mapToProjectResponse(saved);
    }

    @Transactional
    public ProjectResponseDto removeEmployee(Long projectId, Long employeeId, String username) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        validateProjectManagementAccess(project, username);

        UserEntity employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        project.getAssignedEmployees().remove(employee);
        ProjectEntity saved = projectRepository.save(project);
        UserEntity currentUser = getCurrentUser(username);
        auditLogService.logAction(currentUser, "removed_employee", "Project", saved.getName(), "—", employee.getFullName(), saved.getId());

        // Notify removed employee
        notificationService.sendSseNotification(employee.getId(), null, "MEMBER_REMOVED", "Member Removed", "You have been removed from project: " + saved.getName());
        String emailHtml = "<p>Hello " + employee.getFullName() + ",</p><p>You have been removed from the project: <strong>" + saved.getName() + "</strong>.</p>";
        notificationService.sendEmail(employee.getEmail(), "Removed from Project: " + saved.getName(), emailHtml);

        return mapToProjectResponse(saved);
    }

    @Transactional(readOnly = true)
    public void notifyAllMembers(Long projectId, String title, String description, String urgency, String username) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        validateProjectManagementAccess(project, username);

        UserEntity currentUser = getCurrentUser(username);
        String formattedDesc = "From " + currentUser.getFullName() + " (" + currentUser.getRole().getName().name().substring(5).replace("_", " ").toLowerCase() + "): " + description;

        if (project.getManager() != null) {
            notificationService.sendSseNotification(project.getManager().getId(), null, urgency.toUpperCase(), title, formattedDesc);
        }
        if (project.getAssignedEmployees() != null) {
            for (UserEntity employee : project.getAssignedEmployees()) {
                if (employee != null) {
                    notificationService.sendSseNotification(employee.getId(), null, urgency.toUpperCase(), title, formattedDesc);
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public void emailAllMembers(Long projectId, String subject, String body, String username) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));
        validateProjectManagementAccess(project, username);

        UserEntity currentUser = getCurrentUser(username);
        String roleName = currentUser.getRole().getName().name().substring(5).replace("_", " ").toLowerCase();

        String formattedBody = body.replace("\n", "<br/>");

        if (project.getManager() != null) {
            String managerHtml = "<p>Hello " + project.getManager().getFullName() + ",</p>" +
                    "<p>This is a project-wide email broadcast update regarding project <strong>" + project.getName() + "</strong> from <strong>" + currentUser.getFullName() + " (" + roleName + ")</strong>.</p>" +
                    "<hr/>" +
                    "<p>" + formattedBody + "</p>";
            notificationService.sendEmail(project.getManager().getEmail(), subject, managerHtml);
        }
        if (project.getAssignedEmployees() != null) {
            for (UserEntity employee : project.getAssignedEmployees()) {
                if (employee != null) {
                    String employeeHtml = "<p>Hello " + employee.getFullName() + ",</p>" +
                            "<p>This is a project-wide email broadcast update regarding project <strong>" + project.getName() + "</strong> from <strong>" + currentUser.getFullName() + " (" + roleName + ")</strong>.</p>" +
                            "<hr/>" +
                            "<p>" + formattedBody + "</p>";
                    notificationService.sendEmail(employee.getEmail(), subject, employeeHtml);
                }
            }
        }
    }

    public void validateProjectAccess(ProjectEntity project, String username) {
        UserEntity currentUser = getCurrentUser(username);
        RoleType role = currentUser.getRole().getName();

        if (role == RoleType.ROLE_ADMIN) {
            return;
        }

        if (role == RoleType.ROLE_PROJECT_MANAGER) {
            if (project.getManager() == null || !project.getManager().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("Access denied to this project");
            }
            return;
        }

        boolean isAssigned = project.getAssignedEmployees().stream()
                .anyMatch(emp -> emp.getId().equals(currentUser.getId()));
        if (!isAssigned) {
            throw new AccessDeniedException("Access denied to this project");
        }
    }

    public void validateProjectManagementAccess(ProjectEntity project, String username) {
        UserEntity currentUser = getCurrentUser(username);
        RoleType role = currentUser.getRole().getName();

        if (role == RoleType.ROLE_ADMIN) {
            return;
        }

        if (role == RoleType.ROLE_PROJECT_MANAGER) {
            if (project.getManager() == null || !project.getManager().getId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not the manager of this project");
            }
            return;
        }

        throw new AccessDeniedException("Employees cannot perform management actions on projects");
    }

    private UserEntity getCurrentUser(String username) {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Logged-in user not found"));
    }

    public ProjectResponseDto mapToProjectResponse(ProjectEntity project) {
        long totalTasks = taskRepository.countByProject(project);
        long completedTasks = taskRepository.countByProjectAndStatus(project, com.mellinnial.plance.entity.TaskStatus.COMPLETED);
        double progress = totalTasks > 0 ? ((double) completedTasks / totalTasks) * 100.0 : 0.0;

        Set<UserResponseDto> employees = project.getAssignedEmployees() != null ? project.getAssignedEmployees().stream()
                .filter(java.util.Objects::nonNull)
                .map(authService::mapToUserResponse)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet()) : java.util.Collections.emptySet();

        return ProjectResponseDto.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus().name())
                .manager(project.getManager() != null ? authService.mapToUserResponse(project.getManager()) : null)
                .assignedEmployees(employees)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .progressPercentage(progress)
                .build();
    }
}
