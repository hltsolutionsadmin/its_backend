package com.its.authservice.populator;

import com.its.authservice.entity.RoleEntity;
import com.its.authservice.entity.UserEntity;
import com.its.common.dto.UserDTO;
import com.its.common.populator.Populator;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserPopulator implements Populator<UserEntity, UserDTO> {
    @Override
    public void populate(UserEntity source, UserDTO target) {
        if (source == null || target == null) return;
        target.setId(source.getId());
        target.setUsername(source.getUsername());
        target.setEmail(source.getEmail());
        Set<String> roles = source.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet());

    }
}
