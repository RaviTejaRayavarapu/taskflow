package com.taskflow.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequest {

    @Size(min = 1, max = 255, message = "must be between 1 and 255 characters")
    private String name;

    private String description;
}
