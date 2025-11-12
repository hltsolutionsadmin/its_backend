package com.its.userservice.repository;

import com.its.userservice.model.OrganizationUserModel;
import com.its.commonservice.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for OrganizationUserModel
 */
@Repository
public interface OrganizationUserRepository extends JpaRepository<OrganizationUserModel, Long> {
    
    Optional<OrganizationUserModel> findByOrganizationIdAndUserId(Long organizationId, Long userId);
    
    List<OrganizationUserModel> findByOrganizationId(Long organizationId);
    
    List<OrganizationUserModel> findByUserId(Long userId);
    
    boolean existsByOrganizationIdAndUserId(Long organizationId, Long userId);
    
    // Check membership with a specific role
    boolean existsByOrganizationIdAndUserIdAndUserRole_Role(Long organizationId, Long userId, UserRole role);
    
    @Query("SELECT COUNT(ou) FROM OrganizationUserModel ou WHERE ou.organization.id = :orgId")
    long countByOrganizationId(@Param("orgId") Long orgId);
}
