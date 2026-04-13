package com.taskflow.controller;

import com.taskflow.dto.request.CreateTaskRequest;
import com.taskflow.dto.request.UpdateTaskRequest;
import com.taskflow.dto.response.PageResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.security.AuthHelper;
import com.taskflow.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final AuthHelper  authHelper;

    /** GET /projects/:id/tasks?status=todo&priority=high&assignee=uuid&page=0&limit=20 */
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<PageResponse<TaskResponse>> list(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String   status,
            @RequestParam(required = false) String   priority,
            @RequestParam(required = false) UUID     assignee,
            @RequestParam(defaultValue = "0")  int  page,
            @RequestParam(defaultValue = "20") int  limit) {
        return ResponseEntity.ok(
                taskService.list(projectId, authHelper.currentUserId(), status, priority, assignee, page, limit));
    }

    /** POST /projects/:id/tasks */
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskResponse> create(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskService.create(projectId, authHelper.currentUserId(), req));
    }

    /** PATCH /tasks/:id */
    @PatchMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest req) {
        return ResponseEntity.ok(taskService.update(id, authHelper.currentUserId(), req));
    }

    /** DELETE /tasks/:id */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id, authHelper.currentUserId());
        return ResponseEntity.noContent().build();
    }
}
