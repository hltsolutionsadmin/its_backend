package com.example.incuserservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating an organization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequestDTO {
    
    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 200, message = "Organization name must be between 2 and 200 characters")
    private String name;
    
    @NotBlank(message = "Organization code is required")
    @Size(min = 2, max = 20, message = "Organization code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Organization code must contain only uppercase letters, numbers, and underscores")
    private String orgCode;
    
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
    
    @Size(max = 200, message = "Website cannot exceed 200 characters")
    private String website;
    
    @Size(max = 200, message = "Address cannot exceed 200 characters")
    private String address;
    
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;
    
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    private String country;
}
