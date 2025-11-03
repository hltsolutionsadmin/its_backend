package com.example.issueservice.service;

import com.example.issueservice.dto.*;
import com.example.issueservice.model.*;
import com.example.issueservice.repository.*;
import com.its.commonservice.enums.AssignmentType;
import com.its.commonservice.enums.CommentType;
import com.its.commonservice.enums.TicketStatus;
import com.its.commonservice.exception.ErrorCode;
import com.its.commonservice.exception.HltCustomerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for ticket management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final CommentRepository commentRepository;
    private final TicketHistoryRepository historyRepository;
    private final GroupRepository groupRepository;
    private final GroupHistoryRepository groupHistoryRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public TicketDTO createTicket(Long orgId, Long projectId, CreateTicketRequestDTO request, Long reporterId) {
        log.info("Creating ticket in project {} by user {}", projectId, reporterId);
        
        ProjectModel project = projectRepository.findById(projectId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));
        
        if (!project.getOrganizationId().equals(orgId)) {
            throw new HltCustomerException(ErrorCode.FORBIDDEN);
        }
        
        // Generate ticket number
        String ticketNumber = generateTicketNumber(project.getProjectCode());
        String ticketCode = generateTicketCode();
        
        TicketModel ticket = new TicketModel();
        ticket.setTicketNumber(ticketNumber);
        ticket.setTicketCode(ticketCode);
        ticket.setOrganizationId(orgId);
        ticket.setProject(project);
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        // classification fields
        ticket.setIssueType(request.getIssueType());
        ticket.setImpact(request.getImpact());
        ticket.setUrgency(request.getUrgency());
        ticket.setSlaType(request.getSlaType());
        // compute priority code from impact x urgency
        String priorityCode = computePriorityCode(request.getImpact(), request.getUrgency());
        ticket.setPriorityCode(priorityCode);
        // map P1..P4 to TicketPriority enum for backward compatibility
        ticket.setPriority(mapPriorityEnum(priorityCode));
        ticket.setReporterId(reporterId);
        ticket.setRequestName(request.getRequestName());
        ticket.setRequestContact(request.getRequestContact());
        ticket.setClientId(request.getClientId());
        ticket.setAssetId(request.getAssetId());
        ticket.setStatus(TicketStatus.NEW);
        // SLA hours and due timestamps
        int[] sla = getSlaHours(priorityCode);
        ticket.setResponseSlaHours(sla[0]);
        ticket.setResolutionSlaHours(sla[1]);
        Instant now = Instant.now();
        ticket.setSlaResponseDueAt(now.plusSeconds(sla[0] * 3600L));
        ticket.setSlaResolutionDueAt(now.plusSeconds(sla[1] * 3600L));
        ticket.setSlaBreached(false);
        ticket.setSlaPaused(false);
        
        // Set category if provided
        if (request.getCategoryId() != null) {
            CategoryModel category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));
            ticket.setCategory(category);
        }
        
        ticket = ticketRepository.save(ticket);
        
        // Create initial history entry
        createHistoryEntry(ticket, reporterId, "status", null, TicketStatus.NEW.name(), "Ticket created");
        
        log.info("Ticket created successfully: {}", ticket.getTicketNumber());
        
        return buildTicketDTO(ticket);
    }

    @Transactional(readOnly = true)
    public TicketDTO getTicketById(Long ticketId) {
        TicketModel ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));
        
        return buildTicketDTO(ticket);
    }

    @Transactional(readOnly = true)
    public Page<TicketDTO> getProjectTickets(Long projectId, Pageable pageable) {
        return ticketRepository.findByProjectId(projectId, pageable)
            .map(this::buildTicketDTO);
    }

    @Transactional
    public void assignTicket(Long ticketId, AssignTicketRequestDTO request, Long assignerId) {
        log.info("Assigning ticket {} by user {}", ticketId, assignerId);
        
        TicketModel ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));
        
        String oldValue = formatAssignment(ticket.getAssignmentType(), ticket.getAssignedUserId(), ticket.getAssignedGroupId());
        
        if (request.getAssignmentType() == AssignmentType.USER) {
            if (request.getUserId() == null) {
                throw new HltCustomerException(ErrorCode.INVALID_ASSIGNMENT, "User ID is required for USER assignment");
            }
            ticket.setAssignmentType(AssignmentType.USER);
            ticket.setAssignedUserId(request.getUserId());
            ticket.setAssignedGroupId(null);
        } else if (request.getAssignmentType() == AssignmentType.GROUP) {
            if (request.getGroupId() == null) {
                throw new HltCustomerException(ErrorCode.INVALID_ASSIGNMENT, "Group ID is required for GROUP assignment");
            }
            GroupModel group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new HltCustomerException(ErrorCode.GROUP_NOT_FOUND));
            
            ticket.setAssignmentType(AssignmentType.GROUP);
            ticket.setAssignedGroupId(request.getGroupId());
            ticket.setAssignedUserId(null);
            // Record group history if group changed
            Long previousGroupId = null;
            if (oldValue != null && oldValue.startsWith("Group:")) {
                try { previousGroupId = Long.parseLong(oldValue.substring("Group:".length())); } catch (Exception ignored) {}
            }
            if (previousGroupId == null || !previousGroupId.equals(request.getGroupId())) {
                GroupHistoryModel gh = new GroupHistoryModel();
                gh.setTicket(ticket);
                gh.setFromGroupId(previousGroupId);
                gh.setToGroupId(request.getGroupId());
                gh.setChangedBy(assignerId);
                gh.setNote(request.getNote());
                groupHistoryRepository.save(gh);
            }
        }
        
        ticketRepository.save(ticket);
        
        String newValue = formatAssignment(ticket.getAssignmentType(), ticket.getAssignedUserId(), ticket.getAssignedGroupId());
        createHistoryEntry(ticket, assignerId, "assignment", oldValue, newValue, "Ticket assigned");
        
        log.info("Ticket {} assigned successfully", ticketId);
    }

    @Transactional
    public void updateTicketStatus(Long ticketId, UpdateTicketStatusRequestDTO request, Long userId) {
        log.info("Updating ticket {} status to {} by user {}", ticketId, request.getStatus(), userId);
        
        TicketModel ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));
        
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(request.getStatus());

        // SLA pause/resume handling
        boolean isPausing = request.getStatus() == TicketStatus.ON_HOLD || request.getStatus() == TicketStatus.AWAITING_USER_INFO;
        boolean wasPaused = Boolean.TRUE.equals(ticket.getSlaPaused());
        if (isPausing && !wasPaused) {
            // compute remaining seconds and clear due timestamps
            Instant now = Instant.now();
            if (ticket.getSlaResponseDueAt() != null) {
                long remaining = Math.max(0, ticket.getSlaResponseDueAt().getEpochSecond() - now.getEpochSecond());
                ticket.setSlaResponseRemainingSeconds(remaining);
            }
            if (ticket.getSlaResolutionDueAt() != null) {
                long remaining = Math.max(0, ticket.getSlaResolutionDueAt().getEpochSecond() - now.getEpochSecond());
                ticket.setSlaResolutionRemainingSeconds(remaining);
            }
            ticket.setSlaPaused(true);
            ticket.setSlaResponseDueAt(null);
            ticket.setSlaResolutionDueAt(null);
        } else if (!isPausing && wasPaused) {
            // resume: recompute due timestamps from now using remaining seconds if present, else SLA hours
            Instant now = Instant.now();
            Long respRem = ticket.getSlaResponseRemainingSeconds();
            Long resoRem = ticket.getSlaResolutionRemainingSeconds();
            if (respRem != null && respRem > 0) {
                ticket.setSlaResponseDueAt(now.plusSeconds(respRem));
            } else if (ticket.getResponseSlaHours() != null) {
                ticket.setSlaResponseDueAt(now.plusSeconds(ticket.getResponseSlaHours() * 3600L));
            }
            if (resoRem != null && resoRem > 0) {
                ticket.setSlaResolutionDueAt(now.plusSeconds(resoRem));
            } else if (ticket.getResolutionSlaHours() != null) {
                ticket.setSlaResolutionDueAt(now.plusSeconds(ticket.getResolutionSlaHours() * 3600L));
            }
            ticket.setSlaPaused(false);
            ticket.setSlaResponseRemainingSeconds(null);
            ticket.setSlaResolutionRemainingSeconds(null);
        }

        // Update timestamps based on terminal statuses
        if (request.getStatus() == TicketStatus.RESOLVED && ticket.getResolvedAt() == null) {
            ticket.setResolvedAt(Instant.now());
        }
        if (request.getStatus() == TicketStatus.CLOSED && ticket.getClosedAt() == null) {
            ticket.setClosedAt(Instant.now());
        }
        
        ticketRepository.save(ticket);
        
        createHistoryEntry(ticket, userId, "status", oldStatus.name(), request.getStatus().name(), 
            "Status changed from " + oldStatus + " to " + request.getStatus());
        
        // Add comment if provided
        if (request.getComment() != null && !request.getComment().isEmpty()) {
            CommentModel comment = new CommentModel();
            comment.setTicket(ticket);
            comment.setAuthorId(userId);
            comment.setText(request.getComment());
            comment.setType(CommentType.COMMENT);
            comment.setIsInternal(false);
            commentRepository.save(comment);
        }
        
        log.info("Ticket {} status updated successfully", ticketId);
    }

    @Transactional
    public CommentDTO addComment(Long ticketId, AddCommentRequestDTO request, Long authorId) {
        log.info("Adding comment to ticket {} by user {}", ticketId, authorId);
        
        TicketModel ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));
        
        CommentModel comment = new CommentModel();
        comment.setTicket(ticket);
        comment.setAuthorId(authorId);
        comment.setText(request.getText());
        comment.setType(request.getType());
        comment.setIsInternal(request.getIsInternal());
        
        comment = commentRepository.save(comment);
        
        log.info("Comment added successfully to ticket {}", ticketId);
        
        return buildCommentDTO(comment);
    }

    @Transactional(readOnly = true)
    public List<CommentDTO> getTicketComments(Long ticketId) {
        return commentRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
            .stream()
            .map(this::buildCommentDTO)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TicketHistoryDTO> getTicketHistory(Long ticketId) {
        return historyRepository.findByTicketIdOrderByCreatedAtDesc(ticketId)
            .stream()
            .map(this::buildHistoryDTO)
            .collect(Collectors.toList());
    }

    private void createHistoryEntry(TicketModel ticket, Long userId, String field, String oldValue, String newValue, String description) {
        TicketHistoryModel history = new TicketHistoryModel();
        history.setTicket(ticket);
        history.setChangedBy(userId);
        history.setFieldName(field);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangeDescription(description);
        historyRepository.save(history);
    }

    private String generateTicketNumber(String projectCode) {
        // Simple implementation - in production, use sequence or counter
        long count = ticketRepository.count() + 1;
        return projectCode + "-" + count;
    }

    private String generateTicketCode() {
        java.time.ZonedDateTime zdt = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC);
        int year = zdt.getYear();
        long seq = ticketRepository.count() + 1; // simplistic; replace with sequence table for concurrency safety
        return String.format("TCK-%d-%04d", year, seq % 10000);
    }

    private String computePriorityCode(String impactRaw, String urgencyRaw) {
        if (impactRaw == null || urgencyRaw == null) return "P3";
        String impact = impactRaw.trim().toUpperCase();
        String urgency = urgencyRaw.trim().toUpperCase();
        // normalize
        switch (impact) { case "LOW": case "MEDIUM": case "HIGH": break; default: impact = "MEDIUM"; }
        switch (urgency) { case "LOW": case "MEDIUM": case "HIGH": case "CRITICAL": break; default: urgency = "MEDIUM"; }
        if (impact.equals("LOW")) {
            if (urgency.equals("LOW")) return "P4";
            if (urgency.equals("MEDIUM")) return "P3";
            if (urgency.equals("HIGH")) return "P3";
            return "P2"; // CRITICAL
        } else if (impact.equals("MEDIUM")) {
            if (urgency.equals("LOW")) return "P3";
            if (urgency.equals("MEDIUM")) return "P3";
            if (urgency.equals("HIGH")) return "P2";
            return "P1"; // CRITICAL
        } else { // HIGH
            if (urgency.equals("LOW")) return "P2";
            if (urgency.equals("MEDIUM")) return "P2";
            return "P1"; // HIGH or CRITICAL
        }
    }

    private int[] getSlaHours(String priorityCode) {
        if (priorityCode == null) return new int[]{4,8};
        switch (priorityCode) {
            case "P1": return new int[]{1,2};
            case "P2": return new int[]{2,4};
            case "P3": return new int[]{4,8};
            case "P4": return new int[]{6,12};
            default: return new int[]{4,8};
        }
    }

    private com.its.commonservice.enums.TicketPriority mapPriorityEnum(String code) {
        if (code == null) return com.its.commonservice.enums.TicketPriority.MEDIUM;
        switch (code) {
            case "P1": return com.its.commonservice.enums.TicketPriority.CRITICAL;
            case "P2": return com.its.commonservice.enums.TicketPriority.HIGH;
            case "P3": return com.its.commonservice.enums.TicketPriority.MEDIUM;
            case "P4": return com.its.commonservice.enums.TicketPriority.LOW;
            default: return com.its.commonservice.enums.TicketPriority.MEDIUM;
        }
    }

    private String formatAssignment(AssignmentType type, Long userId, Long groupId) {
        if (type == null) return "Unassigned";
        if (type == AssignmentType.USER) return "User:" + userId;
        if (type == AssignmentType.GROUP) return "Group:" + groupId;
        return "Unassigned";
    }

    private TicketDTO buildTicketDTO(TicketModel ticket) {
        return TicketDTO.builder()
            .id(ticket.getId())
            .ticketNumber(ticket.getTicketNumber())
            .organizationId(ticket.getOrganizationId())
            .projectId(ticket.getProject().getId())
            .projectName(ticket.getProject().getName())
            .projectCode(ticket.getProject().getProjectCode())
            .title(ticket.getTitle())
            .description(ticket.getDescription())
            .status(ticket.getStatus())
            .priority(ticket.getPriority())
            .reporterId(ticket.getReporterId())
            .requestName(ticket.getRequestName())
            .requestContact(ticket.getRequestContact())
            .clientId(ticket.getClientId())
            .categoryId(ticket.getCategory() != null ? ticket.getCategory().getId() : null)
            .categoryName(ticket.getCategory() != null ? ticket.getCategory().getName() : null)
            .assignmentType(ticket.getAssignmentType())
            .assignedUserId(ticket.getAssignedUserId())
            .assignedGroupId(ticket.getAssignedGroupId())
            .assetId(ticket.getAssetId())
            .dueDate(ticket.getDueDate())
            .resolvedAt(ticket.getResolvedAt())
            .closedAt(ticket.getClosedAt())
            .createdAt(ticket.getCreatedAt())
            .updatedAt(ticket.getUpdatedAt())
            .commentCount(ticket.getComments().size())
            .attachmentCount(ticket.getAttachments().size())
            .build();
    }

    private CommentDTO buildCommentDTO(CommentModel comment) {
        return CommentDTO.builder()
            .id(comment.getId())
            .ticketId(comment.getTicket().getId())
            .authorId(comment.getAuthorId())
            .text(comment.getText())
            .type(comment.getType())
            .isInternal(comment.getIsInternal())
            .createdAt(comment.getCreatedAt())
            .build();
    }

    private TicketHistoryDTO buildHistoryDTO(TicketHistoryModel history) {
        return TicketHistoryDTO.builder()
            .id(history.getId())
            .ticketId(history.getTicket().getId())
            .changedBy(history.getChangedBy())
            .fieldName(history.getFieldName())
            .oldValue(history.getOldValue())
            .newValue(history.getNewValue())
            .changeDescription(history.getChangeDescription())
            .createdAt(history.getCreatedAt())
            .build();
    }
}
