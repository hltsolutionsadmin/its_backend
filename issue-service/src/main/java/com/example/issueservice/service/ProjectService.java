package com.example.issueservice.service;

import com.example.issueservice.dto.CreateProjectRequestDTO;
import com.example.issueservice.dto.ProjectDTO;
import com.example.issueservice.model.ProjectModel;
import com.example.issueservice.repository.ProjectRepository;
import com.juvarya.commonservice.exception.ErrorCode;
import com.juvarya.commonservice.exception.HltCustomerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for project management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional
    public ProjectDTO createProject(Long orgId, CreateProjectRequestDTO request) {
        log.info("Creating project with code: {}", request.getProjectCode());
        
        if (projectRepository.existsByProjectCode(request.getProjectCode())) {
            throw new HltCustomerException(ErrorCode.PROJECT_CODE_TAKEN);
        }
        
        ProjectModel project = new ProjectModel();
        project.setOrganizationId(orgId);
        project.setName(request.getName());
        project.setProjectCode(request.getProjectCode().toUpperCase());
        project.setDescription(request.getDescription());
        project.setManagerId(request.getManagerId());
        project.setActive(true);
        
        project = projectRepository.save(project);
        
        log.info("Project created successfully with ID: {}", project.getId());
        
        return buildProjectDTO(project);
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long projectId) {
        ProjectModel project = projectRepository.findById(projectId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));
        
        return buildProjectDTO(project);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDTO> getOrganizationProjects(Long orgId, Pageable pageable) {
        return projectRepository.findByOrganizationId(orgId, pageable)
            .map(this::buildProjectDTO);
    }

    @Transactional(readOnly = true)
    public Page<ProjectDTO> searchProjects(Long orgId, String search, Pageable pageable) {
        return projectRepository.searchByOrganization(orgId, search, pageable)
            .map(this::buildProjectDTO);
    }

    @Transactional
    public ProjectDTO updateProject(Long projectId, CreateProjectRequestDTO request) {
        log.info("Updating project with ID: {}", projectId);
        
        ProjectModel project = projectRepository.findById(projectId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));
        
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        if (request.getManagerId() != null) {
            project.setManagerId(request.getManagerId());
        }
        
        project = projectRepository.save(project);
        
        log.info("Project updated successfully: {}", projectId);
        
        return buildProjectDTO(project);
    }

    @Transactional
    public void deactivateProject(Long projectId) {
        log.info("Deactivating project with ID: {}", projectId);
        
        ProjectModel project = projectRepository.findById(projectId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));
        
        project.setActive(false);
        projectRepository.save(project);
        
        log.info("Project deactivated successfully: {}", projectId);
    }

    private ProjectDTO buildProjectDTO(ProjectModel project) {
        return ProjectDTO.builder()
            .id(project.getId())
            .organizationId(project.getOrganizationId())
            .name(project.getName())
            .projectCode(project.getProjectCode())
            .description(project.getDescription())
            .managerId(project.getManagerId())
            .active(project.getActive())
            .createdAt(project.getCreatedAt())
            .updatedAt(project.getUpdatedAt())
            .memberCount(project.getMembers() != null ? project.getMembers().size() : 0)
            .ticketCount(project.getTickets() != null ? project.getTickets().size() : 0)
            .build();
    }
}
