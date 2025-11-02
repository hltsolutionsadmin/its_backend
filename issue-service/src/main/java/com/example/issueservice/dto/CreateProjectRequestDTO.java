package com.example.issueservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProjectRequestDTO {
    
    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 200)
    private String name;
    
    @NotBlank(message = "Project code is required")
    @Size(min = 2, max = 20)
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Project code must contain only uppercase letters, numbers, and underscores")
    private String projectCode;
    
    @Size(max = 1000)
    private String description;
    
    @NotNull(message = "Manager ID is required")
    private Long managerId;
}
