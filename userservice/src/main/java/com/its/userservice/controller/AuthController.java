package com.its.userservice.controller;

import com.its.userservice.dto.*;
import com.its.common.dto.UserDTO;
import com.its.userservice.service.impl.AuthService;
import com.its.commonservice.dto.StandardResponse;
import com.its.userservice.service.impl.OrganizationService;
import com.its.userservice.service.impl.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints
 * All responses return StandardResponse<T>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final UserService userService;

    private final OrganizationService organizationService;


    /**
     * Register a new user
     * POST /api/auth/register
     */
    @PostMapping("/register")
    public StandardResponse<UserDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        UserDTO user = authService.register(request);
        return StandardResponse.single(user, "User registered successfully");
    }

    /**
     * Login with username/email and password
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public StandardResponse<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return StandardResponse.single(response, "Login successful");
    }

    /**
     * Login with email and password
     * POST /api/auth/login/email
     */
    @PostMapping("/login/email")
    public StandardResponse<AuthResponseDTO> loginWithEmail(@Valid @RequestBody EmailLoginRequestDTO request) {
        // Reuse existing login flow by mapping email to usernameOrEmail
        LoginRequestDTO mapped = new LoginRequestDTO(request.getEmail(), request.getPassword());
        AuthResponseDTO response = authService.login(mapped);
        return StandardResponse.single(response, "Login successful");
    }

    /**
     * Refresh access token using refresh token
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public StandardResponse<AuthResponseDTO> refreshToken(@RequestParam String refreshToken) {
        AuthResponseDTO response = authService.refreshToken(refreshToken);
        return StandardResponse.single(response, "Token refreshed successfully");
    }

    /**
     * Logout user and revoke refresh tokens
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public StandardResponse<Void> logout(@RequestParam Long userId) {
        authService.logout(userId);
        return StandardResponse.single(null, "Logged out successfully");
    }

    @GetMapping("/{userId}")
    public StandardResponse<UserDTO> getUserById(@PathVariable("userId") Long userId) {
        UserDTO user = userService.getUserById(userId);
        return StandardResponse.single(user);
    }

    @PostMapping("/orgs/{userId}")
    public StandardResponse<OrganizationDTO> createOrganization(
            @Valid @RequestBody CreateOrganizationRequestDTO request,
            @PathVariable("userId") Long userId) {
        OrganizationDTO org = organizationService.createOrganization(request, userId);
        return StandardResponse.single(org, "Organization created successfully");
    }

    @GetMapping("/{orgId}")
    public StandardResponse<OrganizationDTO> getOrganization(
            @PathVariable Long orgId,
            @RequestAttribute("userId") Long userId) {
        OrganizationDTO org = organizationService.getOrganizationById(orgId, userId);
        return StandardResponse.single(org);
    }
}
