package com.its.userservice.service.impl;

import com.its.userservice.model.UserModel;
import com.its.userservice.repository.UserRepository;
import com.its.commonservice.enums.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserModel user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // For now, grant a default MEMBER role authority. Extend to derive per-organization roles if needed.
        Set<GrantedAuthority> authorities = Set.of(new SimpleGrantedAuthority("ROLE_" + UserRole.MEMBER.name()));

        return new User(user.getUsername(), user.getPasswordHash(), authorities);
    }
}
