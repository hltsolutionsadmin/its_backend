package com.example.issueservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Project entity
 */
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_org_project", columnList = "organizationId,projectCode"),
    @Index(name = "idx_project_code", columnList = "projectCode")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long organizationId;  // Reference to Organization in user-service
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(nullable = false, unique = true, length = 20)
    private String projectCode;  // e.g., "PROJ", "DEV"
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Long managerId;  // Reference to User in user-service
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<TicketModel> tickets = new HashSet<>();
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<ProjectMemberModel> members = new HashSet<>();
}
