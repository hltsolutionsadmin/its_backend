package com.example.issueservice.service;

import com.its.common.dto.ProjectDTO;
import com.its.common.dto.ProjectStatsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectService {

    ProjectDTO saveOrUpdateProject(ProjectDTO projectDTO);

    ProjectDTO getProjectById(Long projectId);

    Page<ProjectDTO> fetchProjectsWithFilters(Pageable pageable, Long organisationId, Long clientId, Long managerId, String statusStr);

    void deleteProject(Long projectId);

    Page<ProjectDTO> getProjectsForOrganization(Long organizationId, Pageable pageable);

    ProjectStatsDTO getProjectStats();

}
