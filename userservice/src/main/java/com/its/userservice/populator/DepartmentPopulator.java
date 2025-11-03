package com.its.userservice.populator;

import com.its.userservice.dto.DepartmentDTO;
import com.its.userservice.model.DepartmentModel;
import org.springframework.stereotype.Component;

/**
 * Populator for converting DepartmentModel to DepartmentDTO
 */
@Component
public class DepartmentPopulator {
    
    public DepartmentDTO populate(DepartmentModel source) {
        if (source == null) return null;
        
        return DepartmentDTO.builder()
            .id(source.getId())
            .name(source.getName())
            .description(source.getDescription())
            .organizationId(source.getOrganization().getId())
            .headId(source.getHead() != null ? source.getHead().getId() : null)
            .headName(source.getHead() != null ? source.getHead().getUsername() : null)
            .active(source.getActive())
            .createdAt(source.getCreatedAt())
            .build();
    }
}
