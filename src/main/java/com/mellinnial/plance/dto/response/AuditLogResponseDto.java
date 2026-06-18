package com.mellinnial.plance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponseDto {
    private String id;
    private String userId;
    private String action;
    private String entity;
    private String entityName;
    private String timestamp;
    private String oldValue;
    private String newValue;
    private String projectId;
}
