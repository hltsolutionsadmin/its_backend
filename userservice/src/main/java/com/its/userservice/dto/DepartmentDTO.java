package com.its.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for department information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDTO {
    
    private Long id;
    private String name;
    private String description;
    private Long organizationId;
    private Long headId;
    private String headName;
    private Boolean active;
    private Instant createdAt;
}
