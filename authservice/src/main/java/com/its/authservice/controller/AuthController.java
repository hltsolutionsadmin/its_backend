package com.its.authservice.controller;

import com.its.authservice.service.AuthService;
import com.its.common.dto.AuthRequestDTO;
import com.its.common.dto.AuthResponseDTO;
import com.its.common.dto.RegisterRequestDTO;
import com.its.common.dto.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
