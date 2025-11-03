package com.example.issueservice.dto;

import com.example.issueservice.enums.ProjectStatus;
import com.example.issueservice.enums.ProjectType;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequestDTO {
    
    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 200)
    private String name;
    
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Project code must contain only uppercase letters, numbers, and underscores")
    private String projectCode;
    
    @Size(max = 1000)
    private String description;
    
    @NotNull(message = "Manager ID is required")
    @JsonAlias({"projectManagerId"})
    private Long managerId;

    private ProjectStatus status;
    private ProjectType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate targetEndDate;
    private LocalDate dueDate;
    private Long ownerOrganizationId;
    private Long clientOrganizationId;
    private Long clientId;
    private Integer progressPercentage;
}
