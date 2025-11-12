package com.its.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ProjectStatsDTO {
    private long totalProjects;
    private long activeProjects;
    private long completedProjects;
    private long onHoldProjects;
}
