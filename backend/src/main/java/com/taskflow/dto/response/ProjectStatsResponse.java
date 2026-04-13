package com.taskflow.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data @Builder
public class ProjectStatsResponse {
    private long              total;
    private Map<String, Long> byStatus;
    private List<AssigneeCount> byAssignee;

    @Data @Builder
    public static class AssigneeCount {
        private UUID   userId;
        private String name;
        private long   count;
    }
}
