package com.its.userservice.model;

import com.its.commonservice.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Junction table for User-Organization relationship with role
 * Allows a user to be part of multiple organizations with different roles
 */
@Entity
@Table(name = "organization_users", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "user_id"}),
    indexes = {
        @Index(name = "idx_org_user", columnList = "organization_id,user_id"),
        @Index(name = "idx_user_org", columnList = "user_id,organization_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUserModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationModel organization;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserModel user;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;  // Role within this organization
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant joinedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_by")
    private UserModel invitedBy;
}
