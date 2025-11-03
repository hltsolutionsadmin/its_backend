package com.its.userservice.repository;

import com.its.userservice.model.OrganizationModel;
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
