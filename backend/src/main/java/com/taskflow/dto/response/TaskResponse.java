package com.taskflow.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.taskflow.model.Task;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data @Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskResponse {
    private UUID      id;
    private String    title;
    private String    description;
    private String    status;
    private String    priority;
    private UUID      projectId;
    private UUID      assigneeId;
    private String    assigneeName;
    private UUID      creatorId;
    private LocalDate dueDate;
    private Instant   createdAt;
    private Instant   updatedAt;

    public static TaskResponse from(Task t) {
        return TaskResponse.builder()
                .id(t.getId()).title(t.getTitle()).description(t.getDescription())
                .status(t.getStatus().name().toLowerCase())
                .priority(t.getPriority().name().toLowerCase())
                .projectId(t.getProject().getId())
                .assigneeId(t.getAssignee() != null ? t.getAssignee().getId() : null)
                .assigneeName(t.getAssignee() != null ? t.getAssignee().getName() : null)
                .creatorId(t.getCreator().getId())
                .dueDate(t.getDueDate()).createdAt(t.getCreatedAt()).updatedAt(t.getUpdatedAt())
                .build();
    }
}
