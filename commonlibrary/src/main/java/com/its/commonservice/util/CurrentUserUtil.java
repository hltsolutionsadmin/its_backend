package com.its.commonservice.util;

import com.its.commonservice.dto.CurrentUser;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * Utility to resolve the current authenticated user from a Bearer JWT.
 */
@Component
@RequiredArgsConstructor
public class CurrentUserUtil {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    /**
     * Resolve CurrentUser from HttpServletRequest's Authorization header (Bearer token).
     * Returns Optional.empty() if header missing/invalid.
     */
    public Optional<CurrentUser> resolveFrom(HttpServletRequest request) {
        String token = extractBearerToken(request);
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        try {
            return Optional.of(resolveFromToken(token));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * Resolve CurrentUser from a raw JWT token string.
     * Throws HltCustomerException if token is invalid/expired.
     */
    public CurrentUser resolveFromToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);
            Long orgId = jwtUtil.extractOrgId(token);
            List<String> roles = jwtUtil.extractRoles(token);

            if (userId == null || !StringUtils.hasText(username)) {
                throw new HltCustomerException(ErrorCode.TOKEN_INVALID);
            }

            return CurrentUser.builder()
                    .id(userId)
                    .username(username)
                    .roles(roles)
                    .orgId(orgId)
                    .build();
        } catch (Exception ex) {
            throw new HltCustomerException(ErrorCode.TOKEN_INVALID);
        }
    }

    /**
     * Same as resolveFrom(HttpServletRequest) but throws if missing/invalid.
     */
    public CurrentUser require(HttpServletRequest request) {
        return resolveFrom(request).orElseThrow(() -> new HltCustomerException(ErrorCode.TOKEN_INVALID));
    }

    private String extractBearerToken(HttpServletRequest request) {
        if (request == null) return null;
        String header = request.getHeader(AUTH_HEADER);
        if (!StringUtils.hasText(header)) {
            return null;
        }
        if (header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length()).trim();
        }
        return null;
    }
}
