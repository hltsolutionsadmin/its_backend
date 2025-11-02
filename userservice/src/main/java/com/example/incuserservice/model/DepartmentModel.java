package com.example.incuserservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Department entity within an organization
 */
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_org_dept", columnList = "organization_id,name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationModel organization;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_id")
    private UserModel head;  // Department head
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
