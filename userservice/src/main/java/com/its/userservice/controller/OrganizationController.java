package com.its.userservice.controller;

import com.its.common.dto.UserDTO;
import com.its.userservice.dto.CreateOrganizationRequestDTO;
import com.its.userservice.dto.InviteUserRequestDTO;
import com.its.userservice.dto.OrganizationDTO;
import com.its.userservice.model.UserModel;
import com.its.userservice.service.impl.OrganizationService;
import com.its.commonservice.dto.StandardResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
            @RequestParam("userId") Long userId) {
        OrganizationDTO org = organizationService.createOrganization(request, userId);
        return StandardResponse.single(org, "Organization created successfully");
    }

    /**
     * Get organizationgit by ID
     * GET /api/orgs/{orgId}
     */
    @GetMapping("/{orgId}")
    public StandardResponse<OrganizationDTO> getOrganization(
            @PathVariable("orgId") Long orgId,
            @RequestParam("userId") Long userId) {  // 👈 explicitly specify name here
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
    @PutMapping("update/{orgId}")
    public StandardResponse<OrganizationDTO> updateOrganization(
            @PathVariable ("orgId") Long orgId,
            @Valid @RequestBody CreateOrganizationRequestDTO request,
            @RequestParam("userId") Long userId) {
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
            @PathVariable("orgId") Long orgId,
            @Valid @RequestBody InviteUserRequestDTO request,
            @RequestAttribute("userId") Long userId,
            @RequestParam("newUserId") Long newUserId) {
        organizationService.inviteUserToOrganization(orgId, request, userId,newUserId);
        return StandardResponse.single(null, "User invited successfully");
    }

    /**
     * Remove user from organization
     * DELETE /api/orgs/{orgId}/users/{userId}
     * Requires ORG_ADMIN role
     */
    @DeleteMapping("/{orgId}/users/{targetUserId}")
    public StandardResponse<Void> removeUser(
            @PathVariable ("orgId") Long orgId,
            @PathVariable ("targetUserId") Long targetUserId,
            @RequestAttribute("userId") Long userId) {
        organizationService.removeUserFromOrganization(orgId, targetUserId, userId);
        return StandardResponse.single(null, "User removed from organization successfully");
    }

    @GetMapping("/{organizationId}/users")
    public StandardResponse<UserDTO> getUsersByOrganization(
            @PathVariable("organizationId") Long organizationId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Page<UserDTO> userDTO=organizationService.getUsersByOrganizationId(organizationId, page, size);
        return StandardResponse.page(userDTO);
    }
}
