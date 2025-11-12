package com.its.userservice.populator;


import com.its.common.dto.ProjectDTO;

import com.its.common.dto.UserGroupDTO;
import com.its.common.populator.Populator;
import com.its.commonservice.dto.StandardResponse;
import com.its.common.dto.UserDTO;
import com.its.userservice.model.UserGroupModel;
import com.its.userservice.client.IssueServiceClient;
import org.springframework.stereotype.Component;

@Component
public class UserGroupPopulator implements Populator<UserGroupModel, UserGroupDTO> {

    private final UserPopulator userPopulator;
    private final IssueServiceClient issueServiceClient;

    public UserGroupPopulator(UserPopulator userPopulator, IssueServiceClient issueServiceClient) {
        this.userPopulator = userPopulator;
        this.issueServiceClient = issueServiceClient;
    }

    @Override
    public void populate(UserGroupModel source, UserGroupDTO target) {
        if (source == null || target == null) {
            return;
        }

        target.setId(source.getId());
        target.setGroupName(source.getGroupName());
        target.setDescription(source.getDescription());
        target.setPriority(source.getPriority());

        // Fetch Project details from issue-service via Feign if projectId is present
        if (source.getProjectId() != null) {
            ProjectDTO projectDTO = fetchProject(source.getProjectId());
            target.setProject(projectDTO);
        }

        if (source.getGroupLead() != null) {
            UserDTO leadDTO = new UserDTO();
            leadDTO=userPopulator.populate(source.getGroupLead());
            target.setGroupLead(leadDTO);
        }
    }

    public UserGroupDTO toDTO(UserGroupModel source) {
        UserGroupDTO dto = new UserGroupDTO();
        populate(source, dto);
        return dto;
    }

    private ProjectDTO fetchProject(Long projectId) {
        StandardResponse<ProjectDTO> response = issueServiceClient.getProjectById(projectId);
        return response != null ? response.getData() : null;
    }
}
