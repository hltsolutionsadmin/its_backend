package com.example.issueservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Client entity - external clients who can create service requests
 */
@Entity
@Table(name = "clients", indexes = {
    @Index(name = "idx_org_client_email", columnList = "organizationId,email")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long organizationId;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(nullable = false, length = 200)
    private String email;
    
    @Column(length = 20)
    private String phone;
    
    @Column(length = 200)
    private String company;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
