package com.taskflow.repository;

import com.taskflow.model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    /**
     * Projects the user owns OR has tasks assigned to them in.
     */
    @Query("""
        SELECT DISTINCT p FROM Project p
        LEFT JOIN p.tasks t
        WHERE p.owner.id = :userId
           OR t.assignee.id = :userId
        ORDER BY p.createdAt DESC
        """)
    Page<Project> findAccessibleByUserId(@Param("userId") UUID userId, Pageable pageable);
}
