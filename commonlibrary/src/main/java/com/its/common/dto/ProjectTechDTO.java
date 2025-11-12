package com.its.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectTechDTO {
    private Long id;

    private Long projectId;

    @NotBlank(message = "Technology name is required")
    @Size(max = 100, message = "Technology name cannot exceed 100 characters")
    private String technologyName;

    @Size(max = 50, message = "Version cannot exceed 50 characters")
    private String version;
}
