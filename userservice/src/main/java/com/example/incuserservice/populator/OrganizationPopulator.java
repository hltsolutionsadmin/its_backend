package com.example.incuserservice.populator;

import com.example.incuserservice.dto.OrganizationDTO;
import com.example.incuserservice.model.OrganizationModel;
import org.springframework.stereotype.Component;

/**
 * Populator for converting OrganizationModel to OrganizationDTO
 */
@Component
public class OrganizationPopulator {
    
    public OrganizationDTO populate(OrganizationModel source) {
        if (source == null) return null;
        
        return OrganizationDTO.builder()
            .id(source.getId())
            .name(source.getName())
            .orgCode(source.getOrgCode())
            .description(source.getDescription())
            .website(source.getWebsite())
            .address(source.getAddress())
            .city(source.getCity())
            .country(source.getCountry())
            .active(source.getActive())
            .ownerId(source.getOwner().getId())
            .ownerName(source.getOwner().getUsername())
            .createdAt(source.getCreatedAt())
            .memberCount(source.getOrganizationUsers().size())
            .build();
    }
}
