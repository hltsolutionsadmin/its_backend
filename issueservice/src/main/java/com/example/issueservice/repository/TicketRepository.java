package com.example.issueservice.repository;

import com.example.issueservice.model.TicketModel;
import com.its.commonservice.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<TicketModel, Long> {
    
    Optional<TicketModel> findByTicketNumber(String ticketNumber);
    
    Page<TicketModel> findByOrganizationId(Long organizationId, Pageable pageable);
    
    Page<TicketModel> findByProjectId(Long projectId, Pageable pageable);
    
    Page<TicketModel> findByAssignedUserId(Long userId, Pageable pageable);
    
    Page<TicketModel> findByAssignedGroupId(Long groupId, Pageable pageable);
    
    Page<TicketModel> findByReporterId(Long reporterId, Pageable pageable);
    
    Page<TicketModel> findByStatus(TicketStatus status, Pageable pageable);
    
    @Query("SELECT t FROM TicketModel t WHERE t.organizationId = :orgId AND t.status = :status")
    Page<TicketModel> findByOrganizationIdAndStatus(@Param("orgId") Long orgId, @Param("status") TicketStatus status, Pageable pageable);
    
    @Query("SELECT t FROM TicketModel t WHERE t.project.id = :projectId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :search, '%')))" )
    Page<TicketModel> searchByProject(@Param("projectId") Long projectId, @Param("search") String search, Pageable pageable);
    
    @Query("SELECT COUNT(t) FROM TicketModel t WHERE t.project.id = :projectId AND t.status = :status")
    long countByProjectAndStatus(@Param("projectId") Long projectId, @Param("status") TicketStatus status);

    @Query("SELECT t FROM TicketModel t WHERE t.slaBreached = false AND (t.slaPaused IS NULL OR t.slaPaused = false) AND " +
           "( (t.slaResponseDueAt IS NOT NULL AND t.slaResponseDueAt < :now) OR " +
           "  (t.slaResolutionDueAt IS NOT NULL AND t.slaResolutionDueAt < :now) ) AND " +
           "t.status NOT IN (com.its.commonservice.enums.TicketStatus.CLOSED, com.its.commonservice.enums.TicketStatus.RESOLVED)")
    java.util.List<TicketModel> findOverdueSlaTickets(@Param("now") Instant now);
}
