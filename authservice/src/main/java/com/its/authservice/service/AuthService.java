package com.its.authservice.service;

import com.its.authservice.entity.RoleEntity;
import com.its.authservice.entity.UserEntity;
import com.its.authservice.repository.RoleRepository;
import com.its.authservice.repository.UserRepository;
import com.its.common.dto.AuthRequestDTO;
import com.its.common.dto.AuthResponseDTO;
import com.its.common.dto.RegisterRequestDTO;
import com.its.common.dto.UserDTO;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public UserDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        RoleEntity roleUser = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    RoleEntity r = new RoleEntity();
                    r.setName("ROLE_USER");
                    return roleRepository.save(r);
                });
        user.getRoles().add(roleUser);
        user = userRepository.save(user);

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public AuthResponseDTO login(AuthRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", authentication.getAuthorities());
        String token = jwtTokenService.generateToken(request.getUsername(), claims);
        return new AuthResponseDTO(token, "Bearer");
    }
}
