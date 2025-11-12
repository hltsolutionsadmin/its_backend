package com.example.issueservice.repository;


import com.example.issueservice.model.ProjectModel;
import com.its.commonservice.enums.ProjectStatus;
import com.its.commonservice.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectModel, Long>, JpaSpecificationExecutor<ProjectModel> {

    Page<ProjectModel> findByOwnerOrganizationIdAndStatus(Long organisationId, ProjectStatus status, Pageable pageable);

    Page<ProjectModel> findByClientId(Long clientId, Pageable pageable);

    Page<ProjectModel> findByProjectManagerId(Long projectManagerId, Pageable pageable);

    Page<ProjectModel> findByClientIdAndProjectManagerId(Long clientId, Long managerId, Pageable pageable);

    Page<ProjectModel> findByClientIdAndStatus(Long clientId, ProjectStatus status, Pageable pageable);

    Page<ProjectModel> findByProjectManagerIdAndStatus(Long managerId, ProjectStatus status, Pageable pageable);

    Page<ProjectModel> findByClientIdAndProjectManagerIdAndStatus(
            Long clientId, Long managerId, ProjectStatus status, Pageable pageable);

    boolean existsByName(String name);

    @Query("SELECT p FROM ProjectModel p WHERE p.ownerOrganizationId = :orgId OR p.clientOrganizationId = :orgId")
    Page<ProjectModel> findByOrganization(@Param("orgId") Long organizationId, Pageable pageable);

    Optional<ProjectModel> findById( Long targetId);

    boolean existsByProjectCode( String projectCode);

    @Query("SELECT COUNT(p) FROM ProjectModel p")
    long countAllProjects();

    @Query("SELECT COUNT(p) FROM ProjectModel p WHERE p.status = com.its.commonservice.enums.ProjectStatus.IN_PROGRESS")
    long countActiveProjects();

    @Query("SELECT COUNT(p) FROM ProjectModel p WHERE p.status = com.its.commonservice.enums.ProjectStatus.COMPLETED")
    long countCompletedProjects();

    @Query("SELECT COUNT(p) FROM ProjectModel p WHERE p.status = com.its.commonservice.enums.ProjectStatus.ON_HOLD")
    long countOnHoldProjects();

}
