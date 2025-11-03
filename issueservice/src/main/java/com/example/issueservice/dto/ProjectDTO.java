package com.example.issueservice.dto;

import com.example.issueservice.enums.ProjectStatus;
import com.example.issueservice.enums.ProjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {
    
    private Long id;
    private Long organizationId;
    private String name;
    private String projectCode;
    private String description;
    private Long managerId;
    private Boolean active;
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
    private Instant createdAt;
    private Instant updatedAt;
    private Integer memberCount;
    private Integer ticketCount;
}
