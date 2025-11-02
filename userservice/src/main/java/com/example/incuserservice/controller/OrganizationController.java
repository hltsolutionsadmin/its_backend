package com.example.incuserservice.controller;

import com.example.incuserservice.dto.CreateOrganizationRequestDTO;
import com.example.incuserservice.dto.InviteUserRequestDTO;
import com.example.incuserservice.dto.OrganizationDTO;
import com.example.incuserservice.service.OrganizationService;
import com.juvarya.commonservice.dto.StandardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for organization management endpoints
 * All responses return StandardResponse<T>
 */
@RestController
@RequestMapping("/api/orgs")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    /**
     * Create a new organization
     * POST /api/orgs
     * User becomes ORG_ADMIN of the created organization
     */
    @PostMapping
    public StandardResponse<OrganizationDTO> createOrganization(
            @Valid @RequestBody CreateOrganizationRequestDTO request,
            @RequestAttribute("userId") Long userId) {
        
        OrganizationDTO org = organizationService.createOrganization(request, userId);
        return StandardResponse.single(org, "Organization created successfully");
    }

    /**
     * Get organization by ID
     * GET /api/orgs/{orgId}
     */
    @GetMapping("/{orgId}")
    public StandardResponse<OrganizationDTO> getOrganization(
            @PathVariable Long orgId,
            @RequestAttribute("userId") Long userId) {
        
        OrganizationDTO org = organizationService.getOrganizationById(orgId, userId);
        return StandardResponse.single(org);
    }

    /**
     * Get all organizations for current user
     * GET /api/orgs
     */
    @GetMapping
    public StandardResponse<OrganizationDTO> getUserOrganizations(
            @RequestAttribute("userId") Long userId) {
        
        List<OrganizationDTO> orgs = organizationService.getUserOrganizations(userId);
        return StandardResponse.list(orgs);
    }

    /**
     * Update organization details
     * PUT /api/orgs/{orgId}
     * Requires ORG_ADMIN role
     */
    @PutMapping("/{orgId}")
    public StandardResponse<OrganizationDTO> updateOrganization(
            @PathVariable Long orgId,
            @Valid @RequestBody CreateOrganizationRequestDTO request,
            @RequestAttribute("userId") Long userId) {
        
        OrganizationDTO org = organizationService.updateOrganization(orgId, request, userId);
        return StandardResponse.single(org, "Organization updated successfully");
    }

    /**
     * Invite user to organization
     * POST /api/orgs/{orgId}/users
     * Requires ORG_ADMIN role
     */
    @PostMapping("/{orgId}/users")
    public StandardResponse<Void> inviteUser(
            @PathVariable Long orgId,
            @Valid @RequestBody InviteUserRequestDTO request,
            @RequestAttribute("userId") Long userId) {
        
        organizationService.inviteUserToOrganization(orgId, request, userId);
        return StandardResponse.single(null, "User invited successfully");
    }

    /**
     * Remove user from organization
     * DELETE /api/orgs/{orgId}/users/{userId}
     * Requires ORG_ADMIN role
     */
    @DeleteMapping("/{orgId}/users/{targetUserId}")
    public StandardResponse<Void> removeUser(
            @PathVariable Long orgId,
            @PathVariable Long targetUserId,
            @RequestAttribute("userId") Long userId) {
        
        organizationService.removeUserFromOrganization(orgId, targetUserId, userId);
        return StandardResponse.single(null, "User removed from organization successfully");
    }
}
