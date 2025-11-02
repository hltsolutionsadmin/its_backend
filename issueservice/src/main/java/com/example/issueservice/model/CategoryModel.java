package com.example.issueservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Category entity for tickets
 */
@Entity
@Table(name = "categories", indexes = {
    @Index(name = "idx_org_category", columnList = "organizationId,name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long organizationId;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private Set<SubCategoryModel> subCategories = new HashSet<>();
}
