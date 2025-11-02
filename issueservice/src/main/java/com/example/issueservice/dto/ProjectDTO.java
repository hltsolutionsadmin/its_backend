package com.example.issueservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
    private Instant createdAt;
    private Instant updatedAt;
    private Integer memberCount;
    private Integer ticketCount;
}
