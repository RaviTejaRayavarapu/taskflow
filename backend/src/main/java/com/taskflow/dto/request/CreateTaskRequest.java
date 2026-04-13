package com.taskflow.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateTaskRequest {

    @NotBlank(message = "is required")
    @Size(max = 255, message = "must not exceed 255 characters")
    private String title;

    private String description;

    @Pattern(regexp = "todo|in_progress|done",
             flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "must be one of: todo, in_progress, done")
    private String status;

    @Pattern(regexp = "low|medium|high",
             flags = Pattern.Flag.CASE_INSENSITIVE,
             message = "must be one of: low, medium, high")
    private String priority;

    private UUID assigneeId;

    private LocalDate dueDate;
}
