package com.example.issueservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Group member entity - users assigned to support groups
 */
@Entity
@Table(name = "group_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}),
    indexes = {
        @Index(name = "idx_group_member", columnList = "group_id,user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupMemberModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupModel group;
    
    @Column(nullable = false)
    private Long userId;  // Reference to User in user-service
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant joinedAt;
}
