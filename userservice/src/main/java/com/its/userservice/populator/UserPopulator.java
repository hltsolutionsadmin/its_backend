package com.its.userservice.populator;

import com.its.common.dto.UserDTO;
import com.its.common.dto.UserOrganizationDTO;
import com.its.userservice.model.OrganizationUserModel;
import com.its.userservice.model.UserModel;
import org.springframework.stereotype.Component;

/**
 * Populator for converting UserModel to UserDTO
 */
@Component
public class UserPopulator {
    
    public UserDTO populate(UserModel source) {
        if (source == null) return null;
        
        return UserDTO.builder()
            .id(source.getId())
            .email(source.getEmail())
            .username(source.getUsername())
            .firstName(source.getFirstName())
            .lastName(source.getLastName())
            .phone(source.getPhone())
            .active(source.getActive())
            .emailVerified(source.getEmailVerified())
            .createdAt(source.getCreatedAt())
            .build();
    }
    
    public UserDTO populateBasic(UserModel source) {
        if (source == null) return null;
        
        return UserDTO.builder()
            .id(source.getId())
            .email(source.getEmail())
            .username(source.getUsername())
            .firstName(source.getFirstName())
            .lastName(source.getLastName())
            .phone(source.getPhone())
            .active(source.getActive())
            .emailVerified(source.getEmailVerified())
            .createdAt(source.getCreatedAt())
            .build();
    }
    
    public UserOrganizationDTO populateOrganization(OrganizationUserModel orgUser) {
        return UserOrganizationDTO.builder()
            .orgId(orgUser.getOrganization().getId())
            .orgName(orgUser.getOrganization().getName())
            .orgCode(orgUser.getOrganization().getOrgCode())
            .role(orgUser.getUserRole().getRole())
            .joinedAt(orgUser.getJoinedAt())
            .build();
    }
}
