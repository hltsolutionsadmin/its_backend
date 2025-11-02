package com.example.issueservice.model;

import com.juvarya.commonservice.enums.GroupLevel;
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
 * Support group entity (L1/L2/L3) for ticket assignment
 */
@Entity
@Table(name = "support_groups", indexes = {
    @Index(name = "idx_org_group", columnList = "organizationId,name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long organizationId;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private GroupLevel level;  // L1, L2, L3
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<GroupMemberModel> members = new HashSet<>();
}
