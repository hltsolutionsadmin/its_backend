package com.example.incuserservice.service;

import com.example.incuserservice.dto.AuthResponseDTO;
import com.example.incuserservice.dto.LoginRequestDTO;
import com.example.incuserservice.dto.RegisterRequestDTO;
import com.example.incuserservice.dto.UserDTO;
import com.example.incuserservice.model.RefreshTokenModel;
import com.example.incuserservice.model.UserModel;
import com.example.incuserservice.populator.UserPopulator;
import com.example.incuserservice.repository.RefreshTokenRepository;
import com.example.incuserservice.repository.UserRepository;
import com.juvarya.commonservice.enums.UserRole;
import com.juvarya.commonservice.exception.ErrorCode;
import com.juvarya.commonservice.exception.HltCustomerException;
import com.juvarya.commonservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

/**
 * Service for authentication operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserPopulator userPopulator;

    @Transactional
    public UserDTO register(RegisterRequestDTO request) {
        log.info("Registering new user with email: {}", request.getEmail());
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS, 
                "User with email " + request.getEmail() + " already exists");
        }
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new HltCustomerException(ErrorCode.USER_ALREADY_EXISTS, 
                "User with username " + request.getUsername() + " already exists");
        }
        
        UserModel user = new UserModel();
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setActive(true);
        user.setEmailVerified(false);
        
        user = userRepository.save(user);
        log.info("User registered successfully with ID: {}", user.getId());
        
        return userPopulator.populateBasic(user);
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        log.info("Login attempt for: {}", request.getUsernameOrEmail());
        
        UserModel user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail())
            .orElseThrow(() -> new HltCustomerException(ErrorCode.INVALID_CREDENTIALS));
        
        if (!user.getActive()) {
            throw new HltCustomerException(ErrorCode.USER_INACTIVE);
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new HltCustomerException(ErrorCode.INVALID_CREDENTIALS);
        }
        
        // Generate tokens
        String accessToken = jwtUtil.generateToken(
            user.getUsername(), 
            user.getId(), 
            Collections.singletonList(UserRole.MEMBER.name()), 
            null
        );
        
        String refreshToken = jwtUtil.generateRefreshToken(user.getUsername(), user.getId());
        
        // Save refresh token
        RefreshTokenModel refreshTokenModel = new RefreshTokenModel();
        refreshTokenModel.setToken(refreshToken);
        refreshTokenModel.setUser(user);
        refreshTokenModel.setExpiryDate(Instant.now().plusMillis(604800000L)); // 7 days
        refreshTokenRepository.save(refreshTokenModel);
        
        log.info("Login successful for user ID: {}", user.getId());
        
        return AuthResponseDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(Collections.singletonList(UserRole.MEMBER.name()))
            .build();
    }

    @Transactional
    public AuthResponseDTO refreshToken(String refreshToken) {
        log.info("Refreshing access token");
        
        RefreshTokenModel tokenModel = refreshTokenRepository.findByToken(refreshToken)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.TOKEN_INVALID));
        
        if (tokenModel.getRevoked()) {
            throw new HltCustomerException(ErrorCode.TOKEN_INVALID, "Token has been revoked");
        }
        
        if (tokenModel.isExpired()) {
            throw new HltCustomerException(ErrorCode.TOKEN_EXPIRED);
        }
        
        UserModel user = tokenModel.getUser();
        
        // Generate new access token
        String accessToken = jwtUtil.generateToken(
            user.getUsername(), 
            user.getId(), 
            Collections.singletonList(UserRole.MEMBER.name()), 
            null
        );
        
        return AuthResponseDTO.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .userId(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .roles(Collections.singletonList(UserRole.MEMBER.name()))
            .build();
    }

    @Transactional
    public void logout(Long userId) {
        log.info("Logging out user: {}", userId);
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}
