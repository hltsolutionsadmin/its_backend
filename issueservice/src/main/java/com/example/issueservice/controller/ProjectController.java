package com.example.issueservice.controller;
import com.example.issueservice.service.ProjectService;
import com.its.common.dto.ProjectDTO;
import com.its.common.dto.ProjectStatsDTO;
import com.its.commonservice.dto.StandardResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/projects")
@AllArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping("/createProject")
    public StandardResponse<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO) {
        ProjectDTO created = projectService.saveOrUpdateProject(projectDTO);
        return StandardResponse.single(created, "Project created successfully");
    }

    @GetMapping("/{projectId}")
    public StandardResponse<ProjectDTO> getProjectById(@PathVariable("projectId") Long projectId) {
        ProjectDTO projectById = projectService.getProjectById(projectId);
        return StandardResponse.single(projectById, "Project fetched successfully");
    }

    @GetMapping
    public StandardResponse<ProjectDTO> getAllProjects(
            Pageable pageable,
            @RequestParam(name = "organisationId", required = false) Long organisationId,
            @RequestParam(name = "clientId", required = false) Long clientId,
            @RequestParam(name = "managerId", required = false) Long managerId,
            @RequestParam(name = "status", required = false) String status
    ) {
        Page<ProjectDTO> projects = projectService.fetchProjectsWithFilters(pageable, organisationId, clientId, managerId, status);
        return StandardResponse.page(projects);
    }

    @DeleteMapping("/{projectId}")
    public StandardResponse<Void> deleteProject(@PathVariable("projectId") Long projectId) {
        projectService.deleteProject(projectId);
        return StandardResponse.message("Project deleted successfully");
    }

    @GetMapping("/organization/{orgId}")
    public StandardResponse<ProjectDTO> getProjectsForOrganization(
            @PathVariable("orgId") Long orgId,
            Pageable pageable
    ) {
        Page<ProjectDTO> projects = projectService.getProjectsForOrganization(orgId, pageable);
        return StandardResponse.page(projects);
    }


    @GetMapping("/stats")
    public ResponseEntity<StandardResponse<ProjectStatsDTO>> getProjectStatistics() {
        ProjectStatsDTO stats = projectService.getProjectStats();
        return ResponseEntity.ok(StandardResponse.single(stats, "Projects stats fetch successful"));
    }
}
