package com.taskflow.dto.response;

import com.taskflow.model.Project;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class ProjectDetailResponse {
    private UUID               id;
    private String             name;
    private String             description;
    private UUID               ownerId;
    private Instant            createdAt;
    private List<TaskResponse> tasks;

    public static ProjectDetailResponse from(Project p, List<TaskResponse> tasks) {
        return ProjectDetailResponse.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .ownerId(p.getOwner().getId()).createdAt(p.getCreatedAt()).tasks(tasks).build();
    }
}
