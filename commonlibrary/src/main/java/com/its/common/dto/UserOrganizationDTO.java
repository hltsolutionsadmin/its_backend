package com.its.common.dto;

import com.its.commonservice.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOrganizationDTO {
    private Long orgId;
    private String orgName;
    private String orgCode;
    private UserRole role;
    private Instant joinedAt;
}