package com.taskflow.service;

import com.taskflow.dto.request.CreateProjectRequest;
import com.taskflow.dto.request.UpdateProjectRequest;
import com.taskflow.dto.response.*;
import com.taskflow.exception.ForbiddenException;
import com.taskflow.exception.NotFoundException;
import com.taskflow.model.Project;
import com.taskflow.model.Task;
import com.taskflow.model.User;
import com.taskflow.repository.ProjectRepository;
import com.taskflow.repository.TaskRepository;
import com.taskflow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository    taskRepository;
    private final UserRepository    userRepository;

    // ── List ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> listAccessible(UUID userId, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Project> result = projectRepository.findAccessibleByUserId(userId, pr);

        return PageResponse.<ProjectResponse>builder()
                .content(result.getContent().stream().map(ProjectResponse::from).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    // ── Create ────────────────────────────────────────────────────────────────

    @Transactional
    public ProjectResponse create(UUID ownerId, CreateProjectRequest req) {
        User owner = resolveUser(ownerId);
        Project project = Project.builder()
                .name(req.getName())
                .description(req.getDescription())
                .owner(owner)
                .build();
        Project saved = projectRepository.saveAndFlush(project);
        log.info("Project created: {} by user {}", saved.getId(), ownerId);
        return ProjectResponse.from(saved);
    }

    // ── Get detail ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProjectDetailResponse getDetail(UUID projectId, UUID requesterId) {
        Project project = resolveProject(projectId);
        checkAccess(project, requesterId);

        List<TaskResponse> tasks = project.getTasks().stream()
                .map(TaskResponse::from)
                .toList();

        return ProjectDetailResponse.from(project, tasks);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    @Transactional
    public ProjectResponse update(UUID projectId, UUID requesterId, UpdateProjectRequest req) {
        Project project = resolveProject(projectId);
        requireOwner(project, requesterId);

        if (req.getName() != null)        project.setName(req.getName());
        if (req.getDescription() != null) project.setDescription(req.getDescription());

        return ProjectResponse.from(projectRepository.save(project));
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Transactional
    public void delete(UUID projectId, UUID requesterId) {
        Project project = resolveProject(projectId);
        requireOwner(project, requesterId);
        projectRepository.delete(project);
        log.info("Project deleted: {} by user {}", projectId, requesterId);
    }

    // ── Stats (bonus) ─────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProjectStatsResponse getStats(UUID projectId, UUID requesterId) {
        Project project = resolveProject(projectId);
        checkAccess(project, requesterId);

        long total = taskRepository.countByProjectId(projectId);

        Map<String, Long> byStatus = taskRepository
                .countByStatusForProject(projectId)
                .stream()
                .collect(Collectors.toMap(
                        row -> ((Task.Status) row[0]).name().toLowerCase(),
                        row -> (Long) row[1]));

        List<ProjectStatsResponse.AssigneeCount> byAssignee = taskRepository
                .countByAssigneeForProject(projectId)
                .stream()
                .map(row -> ProjectStatsResponse.AssigneeCount.builder()
                        .userId((UUID)   row[0])
                        .name((String)   row[1])
                        .count((Long)    row[2])
                        .build())
                .toList();

        return ProjectStatsResponse.builder()
                .total(total)
                .byStatus(byStatus)
                .byAssignee(byAssignee)
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public Project resolveProject(UUID id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("project not found"));
    }

    public User resolveUser(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user not found"));
    }

    /** Owner OR a member who has tasks in the project can read it. */
    private void checkAccess(Project project, UUID requesterId) {
        boolean isOwner  = project.getOwner().getId().equals(requesterId);
        boolean hasTasks = project.getTasks().stream()
                .anyMatch(t -> t.getAssignee() != null
                        && t.getAssignee().getId().equals(requesterId));
        if (!isOwner && !hasTasks) throw new ForbiddenException("access denied");
    }

    /** Only the owner may mutate the project. */
    private void requireOwner(Project project, UUID requesterId) {
        if (!project.getOwner().getId().equals(requesterId)) {
            throw new ForbiddenException("only the project owner can perform this action");
        }
    }
}
