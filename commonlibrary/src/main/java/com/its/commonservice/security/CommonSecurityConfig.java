package com.its.commonservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class CommonSecurityConfig {

    @Value("${security.permit.api-all:true}")
    private boolean permitApiAll;

    @Value("${security.permit.paths:}")
    private String extraPermitPaths;

    @Bean
    @ConditionalOnMissingBean(SecurityFilterChain.class)
    public SecurityFilterChain commonSecurityFilterChain(HttpSecurity http) throws Exception {
        // Build permitted paths list
        List<String> permit = new ArrayList<>();
        permit.addAll(Arrays.asList(
                "/error",
                "/actuator/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html"
        ));
        if (permitApiAll) {
            permit.add("/api/**");
        }
        if (extraPermitPaths != null && !extraPermitPaths.isBlank()) {
            for (String p : extraPermitPaths.split(",")) {
                String trimmed = p.trim();
                if (!trimmed.isEmpty()) permit.add(trimmed);
            }
        }

        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(permit.toArray(String[]::new)).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        return http.build();
    }
}
