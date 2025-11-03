package com.its.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for authentication response with JWT tokens
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String email;
    private List<String> roles;
    private Long currentOrgId;  // Default organization if user has one
}
