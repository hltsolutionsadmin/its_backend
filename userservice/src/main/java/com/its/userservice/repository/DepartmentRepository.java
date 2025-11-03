package com.its.userservice.repository;

import com.its.userservice.model.DepartmentModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for DepartmentModel
 */
@Repository
public interface DepartmentRepository extends JpaRepository<DepartmentModel, Long> {
    
    List<DepartmentModel> findByOrganizationId(Long organizationId);
    
    Optional<DepartmentModel> findByOrganizationIdAndName(Long organizationId, String name);
    
    boolean existsByOrganizationIdAndName(Long organizationId, String name);
}
