package com.taskflow.service;

import com.taskflow.dto.request.CreateTaskRequest;
import com.taskflow.dto.request.UpdateTaskRequest;
import com.taskflow.dto.response.PageResponse;
import com.taskflow.dto.response.TaskResponse;
import com.taskflow.exception.ForbiddenException;
import com.taskflow.exception.NotFoundException;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository    taskRepository;
    private final UserRepository    userRepository;
    private final ProjectService    projectService;

    // ── List ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<TaskResponse> list(UUID projectId, UUID requesterId,
                                           String statusStr, String priorityStr, UUID assigneeId,
                                           int page, int size) {
        Project project = projectService.resolveProject(projectId);

        Task.Status status = StringUtils.hasText(statusStr)
                ? Task.Status.fromString(statusStr) : null;
        
        Task.Priority priority = StringUtils.hasText(priorityStr)
                ? Task.Priority.fromString(priorityStr) : null;

        PageRequest pr = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Task> result = taskRepository.findByProjectIdWithFilters(
                projectId, status, priority, assigneeId, pr);

        return PageResponse.<TaskResponse>builder()
                .content(result.getContent().stream().map(TaskResponse::from).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public TaskResponse create(UUID projectId, UUID creatorId, CreateTaskRequest req) {
        Project project = projectService.resolveProject(projectId);
        User    creator = projectService.resolveUser(creatorId);

        User assignee = null;
        if (req.getAssigneeId() != null) {
            assignee = userRepository.findById(req.getAssigneeId())
                    .orElseThrow(() -> new NotFoundException("assignee not found"));
        }

        Task task = Task.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .status(req.getStatus() != null
                        ? Task.Status.fromString(req.getStatus()) : Task.Status.TODO)
                .priority(req.getPriority() != null
                        ? Task.Priority.fromString(req.getPriority()) : Task.Priority.MEDIUM)
                .project(project)
                .creator(creator)
                .assignee(assignee)
                .dueDate(req.getDueDate())
                .build();

        Task saved = taskRepository.saveAndFlush(task);
        log.info("Task created: {} in project {}", saved.getId(), projectId);
        return TaskResponse.from(saved);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public TaskResponse update(UUID taskId, UUID requesterId, UpdateTaskRequest req) {
        Task task = resolveTask(taskId);

        // Anyone who can see the project may update a task; owner-only delete is separate.
        if (StringUtils.hasText(req.getTitle()))       task.setTitle(req.getTitle());
        if (req.getDescription() != null)              task.setDescription(req.getDescription());
        if (StringUtils.hasText(req.getStatus()))      task.setStatus(Task.Status.fromString(req.getStatus()));
        if (StringUtils.hasText(req.getPriority()))    task.setPriority(Task.Priority.fromString(req.getPriority()));

        if (req.isClearAssignee()) {
            task.setAssignee(null);
        } else if (req.getAssigneeId() != null) {
            User assignee = userRepository.findById(req.getAssigneeId())
                    .orElseThrow(() -> new NotFoundException("assignee not found"));
            task.setAssignee(assignee);
        }

        if (req.isClearDueDate()) {
            task.setDueDate(null);
        } else if (req.getDueDate() != null) {
            task.setDueDate(req.getDueDate());
        }

        return TaskResponse.from(taskRepository.save(task));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(UUID taskId, UUID requesterId) {
        Task task = resolveTask(taskId);

        boolean isProjectOwner = task.getProject().getOwner().getId().equals(requesterId);
        boolean isCreator      = task.getCreator().getId().equals(requesterId);

        if (!isProjectOwner && !isCreator) {
            throw new ForbiddenException("only the project owner or task creator can delete this task");
        }

        taskRepository.delete(task);
        log.info("Task deleted: {} by user {}", taskId, requesterId);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Task resolveTask(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("task not found"));
    }
}
