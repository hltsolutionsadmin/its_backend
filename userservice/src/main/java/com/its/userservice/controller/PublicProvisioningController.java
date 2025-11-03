package com.its.userservice.controller;

import com.its.commonservice.dto.StandardResponse;
import com.its.userservice.dto.OrganizationDTO;
import com.its.userservice.dto.ProvisionOrganizationRequestDTO;
import com.its.userservice.service.ProvisioningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoints for initial organization provisioning
 */
@RestController
@RequestMapping("/api/public/provision")
@RequiredArgsConstructor
public class PublicProvisioningController {

    private final ProvisioningService provisioningService;

    /**
     * Provision an organization and its initial admin user
     * POST /api/public/provision/org
     */
    @PostMapping("/org")
    public StandardResponse<OrganizationDTO> provisionOrganization(
            @Valid @RequestBody ProvisionOrganizationRequestDTO request) {
        OrganizationDTO dto = provisioningService.provisionOrganization(request);
        return StandardResponse.single(dto, "Organization provisioned successfully");
    }
}
