package com.example.issueservice.service;

import com.its.common.dto.ProjectTechDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProjectTechService {

    ProjectTechDTO addTechToProject(Long projectId, ProjectTechDTO dto);

    Page<ProjectTechDTO> getTechStackByProject(Long projectId, Pageable pageable);

    ProjectTechDTO updateTech(Long techId, ProjectTechDTO dto);

    void deleteTech(Long techId);
}
