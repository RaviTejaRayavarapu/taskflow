package com.taskflow.repository;

import com.taskflow.model.Task;
import com.taskflow.model.Task.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    @Query("""
        SELECT t FROM Task t
        WHERE t.project.id = :projectId
          AND (:status   IS NULL OR t.status = :status)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:assignee IS NULL OR t.assignee.id = :assignee)
        ORDER BY t.createdAt DESC
        """)
    Page<Task> findByProjectIdWithFilters(
            @Param("projectId") UUID projectId,
            @Param("status")    Status status,
            @Param("priority")  Task.Priority priority,
            @Param("assignee")  UUID assignee,
            Pageable pageable);

    // Stats: count per status for a project
    @Query("""
        SELECT t.status, COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
        GROUP BY t.status
        """)
    List<Object[]> countByStatusForProject(@Param("projectId") UUID projectId);

    // Stats: count per assignee for a project
    @Query("""
        SELECT t.assignee.id, t.assignee.name, COUNT(t)
        FROM Task t
        WHERE t.project.id = :projectId
          AND t.assignee IS NOT NULL
        GROUP BY t.assignee.id, t.assignee.name
        """)
    List<Object[]> countByAssigneeForProject(@Param("projectId") UUID projectId);

    long countByProjectId(UUID projectId);
}
