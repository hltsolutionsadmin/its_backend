package com.example.issueservice.repository;

import com.example.issueservice.model.CategoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryModel, Long> {
    
    List<CategoryModel> findByOrganizationIdAndActiveTrue(Long organizationId);
}
