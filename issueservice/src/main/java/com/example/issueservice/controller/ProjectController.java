package com.example.issueservice.controller;

import com.example.issueservice.dto.CreateProjectRequestDTO;
import com.example.issueservice.dto.ProjectDTO;
import com.example.issueservice.enums.ProjectStatus;
import com.example.issueservice.enums.ProjectType;
import com.example.issueservice.service.ProjectService;
import com.its.commonservice.dto.StandardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

/**
 * Controller for project management endpoints
 */
@RestController
@RequestMapping("/api/orgs/{orgId}/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public StandardResponse<ProjectDTO> createProject(
            @PathVariable Long orgId,
            @Valid @RequestBody CreateProjectRequestDTO request) {
        
        ProjectDTO project = projectService.createProject(orgId, request);
        return StandardResponse.single(project, "Project created successfully");
    }

    @GetMapping
    public StandardResponse<ProjectDTO> getProjects(
            @PathVariable Long orgId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ProjectStatus status,
            @RequestParam(required = false) ProjectType type,
            @RequestParam(required = false) Long managerId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) LocalDate startDateFrom,
            @RequestParam(required = false) LocalDate startDateTo,
            Pageable pageable) {

        Page<ProjectDTO> projects = projectService.filterProjects(
            orgId, search, status, type, managerId, active, startDateFrom, startDateTo, pageable
        );
        return StandardResponse.page(projects);
    }

    @GetMapping("/{projectId}")
    public StandardResponse<ProjectDTO> getProject(@PathVariable Long projectId) {
        ProjectDTO project = projectService.getProjectById(projectId);
        return StandardResponse.single(project);
    }

    @PutMapping("/{projectId}")
    public StandardResponse<ProjectDTO> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateProjectRequestDTO request) {
        
        ProjectDTO project = projectService.updateProject(projectId, request);
        return StandardResponse.single(project, "Project updated successfully");
    }

    @DeleteMapping("/{projectId}")
    public StandardResponse<Void> deactivateProject(@PathVariable Long projectId) {
        projectService.deactivateProject(projectId);
        return StandardResponse.single(null, "Project deactivated successfully");
    }
}
