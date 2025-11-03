package com.its.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Refresh token for JWT authentication
 */
@Entity
@Table(name = "refresh_tokens", indexes = {
    @Index(name = "idx_token", columnList = "token", unique = true),
    @Index(name = "idx_user_token", columnList = "user_id,token")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 500)
    private String token;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;
    
    @Column(nullable = false)
    private Instant expiryDate;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Boolean revoked = false;
    
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}
