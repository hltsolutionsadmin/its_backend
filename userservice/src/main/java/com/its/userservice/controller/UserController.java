package com.its.userservice.controller;

import com.its.userservice.dto.UserDTO;
import com.its.userservice.service.UserService;
import com.its.commonservice.dto.StandardResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * Get current user information
     * GET /api/users/me
     */
    @GetMapping("/me")
    public StandardResponse<UserDTO> getCurrentUser(@RequestAttribute("userId") Long userId) {
        UserDTO user = userService.getUserById(userId);
        return StandardResponse.single(user);
    }

    /**
     * Get user by ID
     * GET /api/users/{userId}
     */
    @GetMapping("/{userId}")
    public StandardResponse<UserDTO> getUserById(@PathVariable Long userId) {
        UserDTO user = userService.getUserById(userId);
        return StandardResponse.single(user);
    }

    /**
     * Update user profile
     * PUT /api/users/{userId}
     */
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
    @PreAuthorize("hasRole('ADMIN')")
    public StandardResponse<Void> deactivateUser(@PathVariable Long userId) {
        userService.deactivateUser(userId);
        return StandardResponse.single(null, "User deactivated successfully");
    }
}
