package com.mellinnial.plance.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkLogResponseDto {
    private Long id;
    private Long taskId;
    private String taskName;
    private String authorId;
    private String authorName;
    private String message;
    private Double hours;
    private String timestamp;
    private List<String> attachments;
    private List<WorkLogReplyResponseDto> replies;
}
