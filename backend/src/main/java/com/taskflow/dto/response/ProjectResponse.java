package com.taskflow.dto.response;

import com.taskflow.model.Project;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class ProjectResponse {
    private UUID    id;
    private String  name;
    private String  description;
    private UUID    ownerId;
    private Instant createdAt;

    public static ProjectResponse from(Project p) {
        return ProjectResponse.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .ownerId(p.getOwner().getId()).createdAt(p.getCreatedAt()).build();
    }
}
