package com.its.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * DTO for organization information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDTO {
    
    private Long id;
    private String name;
    private String orgCode;
    private String description;
    private String website;
    private String address;
    private String city;
    private String country;
    private Boolean active;
    private Long ownerId;
    private String ownerName;
    private Instant createdAt;
    private Integer memberCount;
}
