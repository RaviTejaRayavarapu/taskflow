package com.taskflow.controller;

import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.request.UpdateProjectRequest;
import com.taskflow.dto.response.*;
import com.taskflow.security.AuthHelper;
import com.taskflow.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final AuthHelper     authHelper;

    /** GET /projects?page=0&limit=20 */
    @GetMapping
    public ResponseEntity<PageResponse<ProjectResponse>> list(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(
                projectService.listAccessible(authHelper.currentUserId(), page, limit));
    }

    /** POST /projects */
    @PostMapping
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.create(authHelper.currentUserId(), req));
    }

    /** GET /projects/:id */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDetailResponse> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getDetail(id, authHelper.currentUserId()));
    }

    /** PATCH /projects/:id */
    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest req) {
        return ResponseEntity.ok(projectService.update(id, authHelper.currentUserId(), req));
    }

    /** DELETE /projects/:id */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        projectService.delete(id, authHelper.currentUserId());
        return ResponseEntity.noContent().build();
    }

    /** GET /projects/:id/stats  (bonus) */
    @GetMapping("/{id}/stats")
    public ResponseEntity<ProjectStatsResponse> stats(@PathVariable UUID id) {
        return ResponseEntity.ok(projectService.getStats(id, authHelper.currentUserId()));
    }
}
