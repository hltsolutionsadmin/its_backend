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
import java.time.Duration;
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
    private final CategoryRepository categoryRepository;
    private final GroupHistoryRepository groupHistoryRepository;
    private final WorkNoteRepository workNoteRepository;

    @Transactional
    public TicketDTO createTicket(Long orgId, Long projectId, CreateTicketRequestDTO request, Long reporterId) {
        log.info("Creating ticket in project {} by user {}", projectId, reporterId);
        
        ProjectModel project = projectRepository.findById(projectId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));
        
        if (!project.getOrganizationId().equals(orgId)) {
            throw new HltCustomerException(ErrorCode.FORBIDDEN);
        }
        
        // Generate ticket number and ticket code
        String ticketNumber = generateTicketNumber(project.getProjectCode());
        String ticketCode = generateTicketCode();
        
        TicketModel ticket = new TicketModel();
        ticket.setTicketNumber(ticketNumber);
        ticket.setTicketCode(ticketCode);
        ticket.setOrganizationId(orgId);
        ticket.setProject(project);
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setIssueType(request.getIssueType());
        // legacy priority if provided
        ticket.setPriority(request.getPriority());
        ticket.setReporterId(reporterId);
        ticket.setRequestName(request.getRequestName());
        ticket.setRequestContact(request.getRequestContact());
        ticket.setClientId(request.getClientId());
        ticket.setAssetId(request.getAssetId());
        ticket.setStatus(TicketStatus.NEW);
        ticket.setImpact(request.getImpact());
        ticket.setUrgency(request.getUrgency());
        ticket.setSlaType(request.getSlaType());
        
        // Set category if provided
        if (request.getCategoryId() != null) {
            CategoryModel category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));
            ticket.setCategory(category);
        }
        
        // Compute priority level and SLA due times
        computePriorityAndSla(ticket);

        // Default assign to L1 group if exists (avoid lambda to keep 'ticket' effectively final)
        java.util.Optional<com.example.issueservice.model.GroupModel> l1Opt =
                groupRepository.findByOrganizationIdAndLevel(orgId, com.its.commonservice.enums.GroupLevel.L1)
                        .stream().findFirst();
        if (l1Opt.isPresent()) {
            GroupModel g = l1Opt.get();
            ticket.setAssignmentType(AssignmentType.GROUP);
            ticket.setAssignedGroupId(g.getId());
        }

        ticket = ticketRepository.save(ticket);
        
        // Create initial history entry
        createHistoryEntry(ticket, reporterId, "status", null, TicketStatus.NEW.name(), "Ticket created");
        if (ticket.getAssignedGroupId() != null) {
            createGroupHistory(ticket, null, ticket.getAssignedGroupId(), reporterId, "Auto-assigned to L1");
        }
        
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
        Long prevGroupId = ticket.getAssignedGroupId();
        
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
        }
        
        ticketRepository.save(ticket);
        
        String newValue = formatAssignment(ticket.getAssignmentType(), ticket.getAssignedUserId(), ticket.getAssignedGroupId());
        createHistoryEntry(ticket, assignerId, "assignment", oldValue, newValue, "Ticket assigned");
        if (prevGroupId == null || (ticket.getAssignedGroupId() != null && !ticket.getAssignedGroupId().equals(prevGroupId))) {
            createGroupHistory(ticket, prevGroupId, ticket.getAssignedGroupId(), assignerId, "Group changed via assignment");
        }
        
        log.info("Ticket {} assigned successfully", ticketId);
    }

    @Transactional
    public void updateTicketStatus(Long ticketId, UpdateTicketStatusRequestDTO request, Long userId) {
        log.info("Updating ticket {} status to {} by user {}", ticketId, request.getStatus(), userId);
        
        TicketModel ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));
        
        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(request.getStatus());
        handleSlaPauseResume(ticket, oldStatus, request.getStatus());
        
        // Update timestamps based on status
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

    @Transactional
    public WorkNoteDTO addWorkNote(Long ticketId, AddWorkNoteRequestDTO request, Long authorId) {
        TicketModel ticket = ticketRepository.findById(ticketId)
            .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));
        WorkNoteModel wn = new WorkNoteModel();
        wn.setTicket(ticket);
        wn.setNote(request.getNote());
        wn.setCreatedBy(authorId);
        wn.setInternalNote(request.isInternal());
        wn = workNoteRepository.save(wn);
        return WorkNoteDTO.builder()
                .id(wn.getId())
                .ticketId(ticket.getId())
                .createdBy(wn.getCreatedBy())
                .note(wn.getNote())
                .internal(wn.getInternalNote())
                .createdAt(wn.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<WorkNoteDTO> getWorkNotes(Long ticketId) {
        return workNoteRepository.findByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(wn -> WorkNoteDTO.builder()
                        .id(wn.getId())
                        .ticketId(wn.getTicket().getId())
                        .createdBy(wn.getCreatedBy())
                        .note(wn.getNote())
                        .internal(wn.getInternalNote())
                        .createdAt(wn.getCreatedAt())
                        .build())
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
        long count = ticketRepository.count() + 1;
        return String.format("TCK-%04d", count);
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
            .ticketCode(ticket.getTicketCode())
            .organizationId(ticket.getOrganizationId())
            .projectId(ticket.getProject().getId())
            .projectName(ticket.getProject().getName())
            .projectCode(ticket.getProject().getProjectCode())
            .title(ticket.getTitle())
            .description(ticket.getDescription())
            .issueType(ticket.getIssueType())
            .status(ticket.getStatus())
            .priority(ticket.getPriority())
            .priorityLevel(ticket.getPriorityLevel())
            .impact(ticket.getImpact())
            .urgency(ticket.getUrgency())
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
            .slaType(ticket.getSlaType())
            .responseSlaHours(ticket.getResponseSlaHours())
            .resolutionSlaHours(ticket.getResolutionSlaHours())
            .slaResponseDueAt(ticket.getSlaResponseDueAt())
            .slaResolutionDueAt(ticket.getSlaResolutionDueAt())
            .slaBreached(ticket.getSlaBreached())
            .slaBreachedAt(ticket.getSlaBreachedAt())
            .slaPaused(ticket.getSlaPaused())
            .createdAt(ticket.getCreatedAt())
            .updatedAt(ticket.getUpdatedAt())
            .commentCount(ticket.getComments().size())
            .attachmentCount(ticket.getAttachments().size())
            .build();
    }

    private void computePriorityAndSla(TicketModel ticket) {
        String impact = safeUpper(ticket.getImpact());
        String urgency = safeUpper(ticket.getUrgency());
        String level = "P3";
        if (impact != null && urgency != null) {
            switch (impact) {
                case "LOW":
                    level = switch (urgency) { case "LOW" -> "P4"; case "MEDIUM" -> "P3"; case "HIGH" -> "P3"; case "CRITICAL" -> "P2"; default -> "P3"; };
                    break;
                case "MEDIUM":
                    level = switch (urgency) { case "LOW" -> "P3"; case "MEDIUM" -> "P3"; case "HIGH" -> "P2"; case "CRITICAL" -> "P1"; default -> "P3"; };
                    break;
                case "HIGH":
                    level = switch (urgency) { case "LOW" -> "P2"; case "MEDIUM" -> "P2"; case "HIGH" -> "P1"; case "CRITICAL" -> "P1"; default -> "P2"; };
                    break;
            }
        }
        ticket.setPriorityLevel(level);
        int response;
        int resolution;
        switch (level) {
            case "P1" -> { response = 1; resolution = 2; }
            case "P2" -> { response = 2; resolution = 4; }
            case "P3" -> { response = 4; resolution = 8; }
            case "P4" -> { response = 6; resolution = 12; }
            default -> { response = 4; resolution = 8; }
        }
        ticket.setResponseSlaHours(response);
        ticket.setResolutionSlaHours(resolution);
        Instant now = Instant.now();
        ticket.setSlaResponseDueAt(now.plus(Duration.ofHours(response)));
        ticket.setSlaResolutionDueAt(now.plus(Duration.ofHours(resolution)));
        ticket.setSlaBreached(false);
        ticket.setSlaPaused(false);
    }

    private void handleSlaPauseResume(TicketModel ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        boolean shouldPause = newStatus == TicketStatus.ON_HOLD || newStatus == TicketStatus.AWAITING_USER_INFO;
        boolean wasPaused = ticket.getSlaPaused() != null && ticket.getSlaPaused();
        Instant now = Instant.now();
        if (shouldPause && !wasPaused) {
            if (ticket.getSlaResponseDueAt() != null) {
                long remaining = Math.max(0, ticket.getSlaResponseDueAt().getEpochSecond() - now.getEpochSecond());
                ticket.setSlaRemainingResponseSeconds(remaining);
                ticket.setSlaResponseDueAt(null);
            }
            if (ticket.getSlaResolutionDueAt() != null) {
                long remaining = Math.max(0, ticket.getSlaResolutionDueAt().getEpochSecond() - now.getEpochSecond());
                ticket.setSlaRemainingResolutionSeconds(remaining);
                ticket.setSlaResolutionDueAt(null);
            }
            ticket.setSlaPaused(true);
        } else if (!shouldPause && wasPaused) {
            if (ticket.getSlaRemainingResponseSeconds() != null) {
                ticket.setSlaResponseDueAt(now.plusSeconds(ticket.getSlaRemainingResponseSeconds()));
                ticket.setSlaRemainingResponseSeconds(null);
            }
            if (ticket.getSlaRemainingResolutionSeconds() != null) {
                ticket.setSlaResolutionDueAt(now.plusSeconds(ticket.getSlaRemainingResolutionSeconds()));
                ticket.setSlaRemainingResolutionSeconds(null);
            }
            ticket.setSlaPaused(false);
        }
    }

    private void createGroupHistory(TicketModel ticket, Long fromGroupId, Long toGroupId, Long changedBy, String note) {
        GroupHistoryModel gh = new GroupHistoryModel();
        gh.setTicket(ticket);
        gh.setFromGroupId(fromGroupId);
        gh.setToGroupId(toGroupId);
        gh.setChangedBy(changedBy);
        gh.setNote(note);
        groupHistoryRepository.save(gh);
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

    private String safeUpper(String s) { return s == null ? null : s.trim().toUpperCase(); }
}
