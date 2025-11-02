package com.example.issueservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Project member entity - users assigned to projects
 */
@Entity
@Table(name = "project_members",
    uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "user_id"}),
    indexes = {
        @Index(name = "idx_project_member", columnList = "project_id,user_id")
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMemberModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectModel project;
    
    @Column(nullable = false)
    private Long userId;  // Reference to User in user-service
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant joinedAt;
}
