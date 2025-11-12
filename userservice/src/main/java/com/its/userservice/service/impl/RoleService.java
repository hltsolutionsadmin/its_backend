package com.its.userservice.service.impl;

import com.its.commonservice.enums.UserRole;
import com.its.userservice.model.RoleModel;
import com.its.userservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public RoleModel getByName(UserRole role) {
        return roleRepository.findByName(role)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + role));
    }
}
