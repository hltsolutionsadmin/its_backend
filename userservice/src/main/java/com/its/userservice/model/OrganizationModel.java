package com.its.userservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Organization entity - multi-tenant organizations
 */
@Entity
    @Table(name = "organizations", indexes = {
    @Index(name = "idx_org_code", columnList = "orgCode", unique = true)
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 200)
    private String name;
    
    @Column(nullable = false, unique = true, length = 20)
    private String orgCode;  // e.g., "ACME", "TECH01"
    
    @Column(length = 1000)
    private String description;
    
    @Column(length = 200)
    private String website;
    
    @Column(length = 200)
    private String address;
    
    @Column(length = 100)
    private String city;
    
    @Column(length = 100)
    private String country;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserModel owner;  // User who created the organization
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DepartmentModel> departments = new HashSet<>();

    @Column(name = "PROJECT_IDS")
    private List<Long> projectIds = new ArrayList<>();
}
