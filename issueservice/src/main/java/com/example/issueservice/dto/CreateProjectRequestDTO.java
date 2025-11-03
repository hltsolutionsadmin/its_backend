package com.example.issueservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.example.issueservice.enums.ProjectStatus;
import com.example.issueservice.enums.ProjectType;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequestDTO {
    
    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 200)
    private String name;
    
    // Optional: if not provided, backend will auto-generate a unique code
    @Size(min = 2, max = 20)
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Project code must contain only uppercase letters, numbers, and underscores")
    private String projectCode;
    
    @Size(max = 1000)
    private String description;
    
    @NotNull(message = "Manager ID is required")
    @JsonAlias({"projectManagerId"})
    private Long managerId;

    // Optional fields
    private ProjectStatus status;      // defaults to PLANNED when null
    private ProjectType type;          // optional
    private LocalDate startDate;       // optional
    private LocalDate endDate;         // optional
    private LocalDate targetEndDate;   // optional
    private LocalDate dueDate;         // optional
    private Long ownerOrganizationId;  // optional
    private Long clientOrganizationId; // optional
    private Long clientId;             // optional
    private Integer progressPercentage;// optional
}
