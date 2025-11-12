package com.example.issueservice.populator;

import com.example.issueservice.model.ProjectModel;
import com.its.common.dto.ProjectDTO;
import com.its.common.dto.ProjectTechDTO;
import com.its.common.populator.Populator;
import com.example.issueservice.client.UserAssignmentClient;
import com.its.common.dto.UserAssignmentDTO;
import com.example.issueservice.repository.ProjectTechRepository;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectPopulator implements Populator<ProjectModel, ProjectDTO> {

    private final ProjectTechPopulator projectTechPopulator;
    private final UserAssignmentClient userAssignmentClient;
    private final ProjectTechRepository projectTechRepository;

    public ProjectPopulator(ProjectTechPopulator projectTechPopulator,
                            UserAssignmentClient userAssignmentClient,
                            ProjectTechRepository projectTechRepository) {
        this.projectTechPopulator = projectTechPopulator;
        this.userAssignmentClient = userAssignmentClient;
        this.projectTechRepository = projectTechRepository;
    }

    @Override
    public void populate(ProjectModel source, ProjectDTO target) {
        if (source == null || target == null) {
            return;
        }

        // Basic fields
        target.setId(source.getId());
        target.setName(source.getName());
        target.setProjectCode(source.getProjectCode());
        target.setDescription(source.getDescription());
        target.setStartDate(source.getStartDate());
        target.setEndDate(source.getEndDate());
        target.setTargetEndDate(source.getTargetEndDate());
        target.setDueDate(source.getDueDate());
        target.setStatus(source.getStatus());
        target.setType(source.getType());
        target.setSlaTier(source.getSlaTier());
        target.setProgressPercentage(source.getProgressPercentage());
        target.setBudgetRange(source.getBudgetRange());
        target.setExpectedTeamSize(source.getExpectedTeamSize());
        target.setArchived(source.getArchived());

        // References
        if (source.getClientId() != null) {
            target.setClientId(source.getClientId());
        }
        if (source.getProjectManagerId() != null) {
            target.setProjectManagerId(source.getProjectManagerId());
        }
        if (source.getOwnerOrganizationId() != null) {
            target.setOwnerOrganizationId(source.getOwnerOrganizationId());
        }
        if (source.getClientOrganizationId() != null) {
            target.setClientOrganizationId(source.getClientOrganizationId());
        }

        // Map Project Members via Feign using user IDs
        if (source.getUserAssignmentIds() != null && !source.getUserAssignmentIds().isEmpty()) {
            List<UserAssignmentDTO> members = new ArrayList<>();
            for (Long userId : source.getUserAssignmentIds()) {
                try {
                    var resp = userAssignmentClient.getAssignmentsByUser(userId, 0, 50, "id", "ASC");
                    if (resp != null && resp.getItems() != null) {
                        members.addAll(resp.getItems());
                    }
                } catch (Exception ignored) {
                    // ignore per-user fetch failure
                }
            }
            target.setProjectMembers(members);
        }

        // Map Technology Stack using repository by IDs
        if (source.getTechnologyStackIds() != null && !source.getTechnologyStackIds().isEmpty()) {
            var techModels = projectTechRepository.findAllById(source.getTechnologyStackIds());
            var techDtos = techModels.stream().map(ptm -> {
                ProjectTechDTO dto = new ProjectTechDTO();
                projectTechPopulator.populate(ptm, dto);
                return dto;
            }).collect(Collectors.toList());
            target.setTechnologyStack(techDtos);
        }
    }

    public ProjectDTO toDTO(ProjectModel source) {
        ProjectDTO dto = new ProjectDTO();
        populate(source, dto);
        return dto;
    }
}
