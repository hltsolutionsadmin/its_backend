package com.its.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role transfer object for role management operations.
 * Contains only user identifier and role name as requested.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleDTO {
    private Long userId;
    private String roleName;
    private Long orgId;
}
