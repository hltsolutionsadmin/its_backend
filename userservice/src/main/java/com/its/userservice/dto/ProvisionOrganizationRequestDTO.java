package com.its.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for provisioning an organization and its initial admin user (public endpoint payload)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProvisionOrganizationRequestDTO {

    // Organization fields from frontend
    @NotBlank(message = "Organization name is required")
    @Size(min = 2, max = 200)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotBlank(message = "Domain name is required")
    @Pattern(regexp = "^[A-Za-z0-9.-]+$", message = "Invalid domain name")
    private String domainName;

    private Boolean active;

    // Admin user fields from frontend
    @NotBlank(message = "Admin full name is required")
    @Size(min = 2, max = 200)
    private String adminFullName;

    @NotBlank(message = "Admin username is required")
    @Size(min = 3, max = 100)
    private String adminUsername;

    @NotBlank(message = "Admin primary contact is required")
    @Size(min = 5, max = 50)
    private String adminPrimaryContact;

    @NotBlank(message = "Admin password is required")
    @Size(min = 6, max = 100)
    private String adminPassword;

    @Email
    @NotBlank(message = "Admin email is required")
    private String email;
}
