package com.example.issueservice.populator;

import com.example.issueservice.model.ProjectTechModel;
import com.its.common.dto.ProjectTechDTO;
import com.its.common.populator.Populator;
import org.springframework.stereotype.Component;

@Component
public class ProjectTechPopulator implements Populator<ProjectTechModel, ProjectTechDTO> {

    @Override
    public void populate(ProjectTechModel source, ProjectTechDTO target) {
        if (source == null || target == null) {
            return;
        }

        target.setId(source.getId());
        target.setTechnologyName(source.getTechnologyName());
        target.setVersion(source.getVersion());
    }
}
