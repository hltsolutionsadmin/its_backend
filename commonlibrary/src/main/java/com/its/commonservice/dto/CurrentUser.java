package com.its.commonservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Lightweight representation of the authenticated user extracted from JWT.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUser {
    private Long id;
    private String username;
    private List<String> roles;
    private Long orgId; // optional, may be null
}
