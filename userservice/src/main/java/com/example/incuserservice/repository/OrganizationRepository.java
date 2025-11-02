package com.example.incuserservice.repository;

import com.example.incuserservice.model.OrganizationModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for OrganizationModel
 */
@Repository
public interface OrganizationRepository extends JpaRepository<OrganizationModel, Long> {
    
    Optional<OrganizationModel> findByOrgCode(String orgCode);
    
    boolean existsByOrgCode(String orgCode);
    
    boolean existsByName(String name);
}
