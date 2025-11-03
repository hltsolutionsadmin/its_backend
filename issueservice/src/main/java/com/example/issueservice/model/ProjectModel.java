package com.example.issueservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import com.example.issueservice.enums.ProjectStatus;
import com.example.issueservice.enums.ProjectType;

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

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ProjectType type;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate targetEndDate;
    private LocalDate dueDate;

    private Long ownerOrganizationId;
    private Long clientOrganizationId;
    private Long clientId;

    @Column
    private Integer progressPercentage;
    
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
