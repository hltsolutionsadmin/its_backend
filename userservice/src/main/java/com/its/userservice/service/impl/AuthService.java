package com.its.userservice.service.impl;

import com.its.userservice.dto.AuthResponseDTO;
import com.its.userservice.dto.LoginRequestDTO;
import com.its.userservice.dto.RegisterRequestDTO;
import com.its.common.dto.UserDTO;
import com.its.userservice.model.RefreshTokenModel;
import com.its.userservice.model.UserModel;
import com.its.userservice.populator.UserPopulator;
import com.its.userservice.repository.RefreshTokenRepository;
import com.its.userservice.repository.UserRepository;
import com.its.commonservice.enums.UserRole;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import com.its.commonservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;

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
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
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
            user.getEmail(),
            user.getId(), 
            Collections.singletonList(UserRole.MEMBER.name()), 
            null
        );
        
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getId());
        
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
