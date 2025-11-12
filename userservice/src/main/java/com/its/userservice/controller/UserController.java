package com.its.userservice.controller;

import com.its.common.dto.UserDTO;
import com.its.commonservice.util.SecurityUtils;
import com.its.userservice.dto.RoleDTO;
import com.its.userservice.model.OrganizationUserModel;
import com.its.userservice.service.impl.OrganizationService;
import com.its.userservice.service.impl.UserService;
import com.its.userservice.service.impl.RoleService;
import com.its.commonservice.dto.StandardResponse;
import com.its.commonservice.enums.UserRole;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for user management endpoints
 * All responses return StandardResponse<T>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrganizationService organizationService;

    @GetMapping("/{userId}")
    public StandardResponse<UserDTO> getUserById(@PathVariable("userId") Long userId) {
        UserDTO user = userService.getUserById(userId);
        return StandardResponse.single(user);
    }

    @GetMapping("{email}/email")
    public StandardResponse<UserDTO> getUserByEmail(@PathVariable("email") String email) {
        UserDTO user = userService.getUserByEmail(email);
        if (user == null) {
            return StandardResponse.error("User not found with email: " + email);
        }
        return StandardResponse.single(user, "User fetched successfully");
    }

    @PutMapping("/{userId}")
    public StandardResponse<UserDTO> updateUser(
            @PathVariable Long userId,
            @RequestAttribute("userId") Long currentUserId,
            @RequestBody UserDTO updateDTO) {
        
        // Users can only update their own profile unless they're admin
        if (!userId.equals(currentUserId)) {
            // Could add admin check here
        }
        
        UserDTO updated = userService.updateUser(userId, updateDTO);
        return StandardResponse.single(updated, "User updated successfully");
    }

    /**
     * Deactivate user account
     * DELETE /api/users/{userId}
     */
    @DeleteMapping("/{userId}")
    public StandardResponse<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return StandardResponse.single(null, "User deactivated successfully");
    }

    /**
     * Create a new user
     * POST /api/users/save
     */
    @PostMapping("/save")
    public StandardResponse<UserDTO> saveUser(@RequestBody UserDTO user) {
        UserDTO saved = userService.saveUser(user);
        return StandardResponse.single(saved, "User created successfully");
    }

    @PostMapping("/roleAdd")
    public StandardResponse<Void> addRole(
            @RequestBody RoleDTO request) {
        if (request.getRoleName() == null || request.getRoleName().isBlank()) {
            throw new HltCustomerException(ErrorCode.INVALID_USER_ROLE);
        }
        organizationService.updateUserOrgRole(request.getOrgId(), request.getUserId(), request.getRoleName());
        return StandardResponse.message("Role added successfully");
    }

    @PutMapping("/roleUpdate")
    public StandardResponse<Void> updateRole(
            @RequestBody RoleDTO request) {
        if (request.getRoleName() == null || request.getRoleName().isBlank()) {
            throw new HltCustomerException(ErrorCode.INVALID_USER_ROLE);
        }
        organizationService.updateUserOrgRole(request.getOrgId(), request.getUserId(), request.getRoleName());
        return StandardResponse.message("Role updated successfully");
    }

    @DeleteMapping("/roleRemove")
    public StandardResponse<Void> removeRole(
            @RequestBody RoleDTO request) {
        if (request.getRoleName() == null || request.getRoleName().isBlank()) {
            throw new HltCustomerException(ErrorCode.INVALID_USER_ROLE);
        }
        organizationService.removeUserOrgRole(request.getOrgId(), request.getUserId(), request.getRoleName());
        return StandardResponse.message("Role removed successfully");
    }

}
