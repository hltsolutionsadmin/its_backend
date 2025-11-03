package com.its.userservice.service;

import com.its.userservice.dto.UserDTO;
import com.its.userservice.model.UserModel;
import com.its.userservice.populator.UserPopulator;
import com.its.userservice.repository.UserRepository;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserPopulator userPopulator;

    @Transactional(readOnly = true)
    public UserDTO getUserById(Long userId) {
        log.debug("Fetching user with ID: {}", userId);
        
        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
        
        return userPopulator.populate(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByEmail(String email) {
        log.debug("Fetching user with email: {}", email);
        
        UserModel user = userRepository.findByEmail(email)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
        
        return userPopulator.populate(user);
    }

    @Transactional(readOnly = true)
    public UserDTO getUserByUsername(String username) {
        log.debug("Fetching user with username: {}", username);
        
        UserModel user = userRepository.findByUsername(username)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
        
        return userPopulator.populate(user);
    }

    @Transactional
    public UserDTO updateUser(Long userId, UserDTO updateDTO) {
        log.info("Updating user with ID: {}", userId);
        
        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
        
        if (updateDTO.getFirstName() != null) {
            user.setFirstName(updateDTO.getFirstName());
        }
        if (updateDTO.getLastName() != null) {
            user.setLastName(updateDTO.getLastName());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        
        user = userRepository.save(user);
        log.info("User updated successfully: {}", userId);
        
        return userPopulator.populate(user);
    }

    @Transactional
    public void deactivateUser(Long userId) {
        log.info("Deactivating user with ID: {}", userId);
        
        UserModel user = userRepository.findById(userId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.USER_NOT_FOUND));
        
        user.setActive(false);
        userRepository.save(user);
        
        log.info("User deactivated successfully: {}", userId);
    }
}
