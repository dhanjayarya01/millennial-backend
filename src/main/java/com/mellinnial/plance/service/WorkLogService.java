package com.mellinnial.plance.service;

import com.mellinnial.plance.dto.request.WorkLogReplyRequestDto;
import com.mellinnial.plance.dto.request.WorkLogRequestDto;
import com.mellinnial.plance.dto.response.WorkLogReplyResponseDto;
import com.mellinnial.plance.dto.response.WorkLogResponseDto;
import com.mellinnial.plance.entity.TaskEntity;
import com.mellinnial.plance.entity.UserEntity;
import com.mellinnial.plance.entity.WorkLogEntity;
import com.mellinnial.plance.entity.WorkLogReplyEntity;
import com.mellinnial.plance.entity.enums.RoleType;
import com.mellinnial.plance.repository.TaskRepository;
import com.mellinnial.plance.repository.UserRepository;
import com.mellinnial.plance.repository.WorkLogReplyRepository;
import com.mellinnial.plance.repository.WorkLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkLogService {

    private final WorkLogRepository workLogRepository;
    private final WorkLogReplyRepository workLogReplyRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public WorkLogResponseDto createWorkLog(WorkLogRequestDto dto, String username) {
        UserEntity author = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        TaskEntity task = taskRepository.findById(dto.getTaskId())
                .orElseThrow(() -> new IllegalArgumentException("Task not found"));

        RoleType role = author.getRole().getName();
        boolean isAssigned = task.getEmployees() != null && task.getEmployees().stream().anyMatch(e -> e.getId().equals(author.getId()));
        if (role != RoleType.ROLE_ADMIN && role != RoleType.ROLE_PROJECT_MANAGER && !isAssigned) {
            throw new AccessDeniedException("You cannot log work on a task you are not assigned to");
        }

        WorkLogEntity workLog = WorkLogEntity.builder()
                .task(task)
                .author(author)
                .message(dto.getMessage())
                .hours(dto.getHours())
                .attachmentUrl(dto.getAttachmentUrl())
                .replies(new ArrayList<>())
                .build();

        WorkLogEntity saved = workLogRepository.save(workLog);

        auditLogService.logAction(author, "logged_work", "WorkLog", task.getName(), "—", dto.getHours() + " hours logged", task.getProject().getId());

        return mapToResponseDto(saved);
    }

    @Transactional
    public WorkLogReplyResponseDto createReply(Long workLogId, WorkLogReplyRequestDto dto, String username) {
        UserEntity author = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("Author not found"));

        WorkLogEntity workLog = workLogRepository.findById(workLogId)
                .orElseThrow(() -> new IllegalArgumentException("Work log not found"));

        WorkLogReplyEntity reply = WorkLogReplyEntity.builder()
                .workLog(workLog)
                .author(author)
                .message(dto.getMessage())
                .build();

        WorkLogReplyEntity saved = workLogReplyRepository.save(reply);

        auditLogService.logAction(author, "replied_to_worklog", "WorkLog", workLog.getTask().getName(), "—", dto.getMessage(), workLog.getTask().getProject().getId());

        return mapToReplyResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkLogResponseDto> getWorkLogs(Long projectId, Long employeeId, String username) {
        UserEntity currentUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        RoleType role = currentUser.getRole().getName();

        List<WorkLogEntity> logs;
        if (role == RoleType.ROLE_ADMIN) {
            logs = workLogRepository.findAll();
        } else if (role == RoleType.ROLE_PROJECT_MANAGER) {
            logs = workLogRepository.findAll().stream()
                    .filter(log -> log.getTask().getProject().getManager() != null && log.getTask().getProject().getManager().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        } else {
            logs = workLogRepository.findAll().stream()
                    .filter(log -> log.getAuthor().getId().equals(currentUser.getId()) ||
                            (log.getTask().getEmployees() != null && log.getTask().getEmployees().stream().anyMatch(e -> e.getId().equals(currentUser.getId()))))
                    .collect(Collectors.toList());
        }

        return logs.stream()
                .filter(log -> projectId == null || log.getTask().getProject().getId().equals(projectId))
                .filter(log -> employeeId == null || log.getAuthor().getId().equals(employeeId))
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private WorkLogResponseDto mapToResponseDto(WorkLogEntity log) {
        return WorkLogResponseDto.builder()
                .id(log.getId())
                .taskId(log.getTask().getId())
                .taskName(log.getTask().getName())
                .authorId(String.valueOf(log.getAuthor().getId()))
                .authorName(log.getAuthor().getFullName())
                .message(log.getMessage())
                .hours(log.getHours())
                .timestamp(log.getTimestamp() != null ? log.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z" : null)
                .attachments(log.getAttachmentUrl() != null && !log.getAttachmentUrl().isEmpty() ? List.of(log.getAttachmentUrl()) : List.of())
                .replies(log.getReplies() != null ? log.getReplies().stream().map(this::mapToReplyResponseDto).collect(Collectors.toList()) : List.of())
                .build();
    }

    private WorkLogReplyResponseDto mapToReplyResponseDto(WorkLogReplyEntity reply) {
        return WorkLogReplyResponseDto.builder()
                .id(reply.getId())
                .authorId(String.valueOf(reply.getAuthor().getId()))
                .authorName(reply.getAuthor().getFullName())
                .message(reply.getMessage())
                .timestamp(reply.getTimestamp() != null ? reply.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z" : null)
                .build();
    }
}
