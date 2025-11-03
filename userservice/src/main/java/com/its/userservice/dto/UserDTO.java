package com.its.userservice.dto;

import com.its.commonservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for user information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean active;
    private Boolean emailVerified;
    private Instant createdAt;
    private List<UserOrganizationDTO> organizations;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserOrganizationDTO {
        private Long orgId;
        private String orgName;
        private String orgCode;
        private UserRole role;
        private Instant joinedAt;
    }
}
