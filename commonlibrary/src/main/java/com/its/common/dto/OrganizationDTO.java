package com.its.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDTO {

    private Long id;

    @NotBlank(message = "Organization name is required")
    @Size(max = 150)
    private String name;

    @Size(max = 1000)
    private String description;

    @Size(max = 100)
    private String domainName;

    private Boolean active = true;

    @NotBlank(message = "Admin full name is required")
    @Size(max = 100)
    private String adminFullName;

    @NotBlank(message = "Admin username is required")
    @Size(max = 50)
    private String adminUsername;

    @NotBlank(message = "Admin primary contact is required")
    @Size(max = 20)
    private String adminPrimaryContact;

    @NotBlank(message = "Admin password is required")
    private String adminPassword;

    // Optional
    @Size(max = 100)
    private String email;
}
