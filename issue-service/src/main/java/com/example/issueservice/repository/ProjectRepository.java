package com.example.issueservice.repository;

import com.example.issueservice.model.ProjectModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectModel, Long> {
    
    Page<ProjectModel> findByOrganizationId(Long organizationId, Pageable pageable);
    
    List<ProjectModel> findByOrganizationId(Long organizationId);
    
    Optional<ProjectModel> findByProjectCode(String projectCode);
    
    boolean existsByProjectCode(String projectCode);
    
    @Query("SELECT p FROM ProjectModel p WHERE p.organizationId = :orgId AND p.active = true")
    Page<ProjectModel> findActiveByOrganizationId(@Param("orgId") Long orgId, Pageable pageable);
    
    @Query("SELECT p FROM ProjectModel p WHERE p.organizationId = :orgId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.projectCode) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProjectModel> searchByOrganization(@Param("orgId") Long orgId, @Param("search") String search, Pageable pageable);
}
