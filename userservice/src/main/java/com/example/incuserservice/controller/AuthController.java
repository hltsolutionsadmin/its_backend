package com.example.incuserservice.controller;

import com.example.incuserservice.dto.AuthResponseDTO;
import com.example.incuserservice.dto.LoginRequestDTO;
import com.example.incuserservice.dto.RegisterRequestDTO;
import com.example.incuserservice.dto.UserDTO;
import com.example.incuserservice.service.AuthService;
import com.juvarya.commonservice.dto.StandardResponse;
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
}
