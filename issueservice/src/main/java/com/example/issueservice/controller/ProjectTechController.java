
package com.example.issueservice.controller;

import com.example.issueservice.service.ProjectTechService;
import com.its.common.dto.ProjectTechDTO;
import com.its.commonservice.dto.StandardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/techstack")
@RequiredArgsConstructor
public class ProjectTechController {

    private final ProjectTechService projectTechService;

    @PostMapping
    public StandardResponse<ProjectTechDTO> addTechToProject(
            @PathVariable Long projectId,
            @Valid @RequestBody ProjectTechDTO dto) {

        ProjectTechDTO created = projectTechService.addTechToProject(projectId, dto);
        return StandardResponse.single(created,"Technology added successfully");
    }


    @GetMapping
    public StandardResponse<ProjectTechDTO> getProjectTechStack(
            @PathVariable Long projectId,
            Pageable pageable) {

        Page<ProjectTechDTO> page = projectTechService.getTechStackByProject(projectId, pageable);
        return StandardResponse.page(page);
    }


    @PutMapping("/{techId}")
    public StandardResponse<ProjectTechDTO> updateTech(
            @PathVariable Long techId,
            @Valid @RequestBody ProjectTechDTO dto) {

        ProjectTechDTO updated = projectTechService.updateTech(techId, dto);
        return StandardResponse.single(updated,"Technology updated successfully");
    }


    @DeleteMapping("/{techId}")
    public StandardResponse<Void> deleteTech(@PathVariable Long techId) {
        projectTechService.deleteTech(techId);
        return StandardResponse.message("Technology removed successfully");
    }
}
