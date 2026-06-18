package com.mellinnial.plance.service;

import com.mellinnial.plance.dto.response.AuditLogResponseDto;
import com.mellinnial.plance.entity.AuditLogEntity;
import com.mellinnial.plance.entity.UserEntity;
import com.mellinnial.plance.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void logAction(UserEntity user, String action, String entity, String entityName, String oldValue, String newValue, Long projectId) {
        AuditLogEntity log = AuditLogEntity.builder()
                .user(user)
                .action(action)
                .entity(entity)
                .entityName(entityName)
                .oldValue(oldValue != null ? oldValue : "—")
                .newValue(newValue != null ? newValue : "—")
                .projectId(projectId)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AuditLogResponseDto mapToDto(AuditLogEntity log) {
        return AuditLogResponseDto.builder()
                .id(String.valueOf(log.getId()))
                .userId(log.getUser() != null ? String.valueOf(log.getUser().getId()) : null)
                .action(log.getAction())
                .entity(log.getEntity())
                .entityName(log.getEntityName())
                .timestamp(log.getTimestamp() != null ? log.getTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z" : null)
                .oldValue(log.getOldValue())
                .newValue(log.getNewValue())
                .projectId(log.getProjectId() != null ? String.valueOf(log.getProjectId()) : null)
                .build();
    }
}
