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
import java.util.Optional;
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
    private final WorkNoteRepository workNoteRepository;

    @Transactional
    public TicketDTO createTicket(Long orgId, Long projectId, CreateTicketRequestDTO request, Long reporterId) {
        log.info("Creating ticket in project {} by user {}", projectId, reporterId);

        ProjectModel project = projectRepository.findById(projectId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.PROJECT_NOT_FOUND));

        if (!project.getOrganizationId().equals(orgId)) {
            throw new HltCustomerException(ErrorCode.FORBIDDEN);
        }

        // Generate ticket number and code
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
        ticket.setImpact(request.getImpact());
        ticket.setUrgency(request.getUrgency());
        ticket.setSlaType(request.getSlaType());

        // Compute priority code and SLA based on impact/urgency
        String priorityCode = computePriorityCode(request.getImpact(), request.getUrgency());
        ticket.setPriorityCode(priorityCode);
        ticket.setPriority(mapPriorityEnum(priorityCode));

        // Set SLA timings
        int[] sla = getSlaHours(priorityCode);
        ticket.setResponseSlaHours(sla[0]);
        ticket.setResolutionSlaHours(sla[1]);
        Instant now = Instant.now();
        ticket.setSlaResponseDueAt(now.plusSeconds(sla[0] * 3600L));
        ticket.setSlaResolutionDueAt(now.plusSeconds(sla[1] * 3600L));
        ticket.setSlaBreached(false);
        ticket.setSlaPaused(false);

        ticket.setReporterId(reporterId);
        ticket.setRequestName(request.getRequestName());
        ticket.setRequestContact(request.getRequestContact());
        ticket.setClientId(request.getClientId());
        ticket.setAssetId(request.getAssetId());
        ticket.setStatus(TicketStatus.NEW);

        // Set category if provided
        if (request.getCategoryId() != null) {
            CategoryModel category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new HltCustomerException(ErrorCode.CATEGORY_NOT_FOUND));
            ticket.setCategory(category);
        }

        // Default assign to L1 group if exists
        Optional<GroupModel> l1Opt = groupRepository
                .findByOrganizationIdAndLevel(orgId, com.its.commonservice.enums.GroupLevel.L1)
                .stream().findFirst();

        l1Opt.ifPresent(g -> {
            ticket.setAssignmentType(AssignmentType.GROUP);
            ticket.setAssignedGroupId(g.getId());
        });

        ticket = ticketRepository.save(ticket);

        // Initial history entries
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
        return ticketRepository.findByProjectId(projectId, pageable).map(this::buildTicketDTO);
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

            // Record group change
            if (prevGroupId == null || !prevGroupId.equals(request.getGroupId())) {
                createGroupHistory(ticket, prevGroupId, request.getGroupId(), assignerId, request.getNote());
            }
        }

        ticketRepository.save(ticket);
        createHistoryEntry(ticket, assignerId, "assignment", oldValue,
                formatAssignment(ticket.getAssignmentType(), ticket.getAssignedUserId(), ticket.getAssignedGroupId()),
                "Ticket assigned");
    }

    @Transactional
    public void updateTicketStatus(Long ticketId, UpdateTicketStatusRequestDTO request, Long userId) {
        log.info("Updating ticket {} status to {} by user {}", ticketId, request.getStatus(), userId);

        TicketModel ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new HltCustomerException(ErrorCode.TICKET_NOT_FOUND));

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(request.getStatus());
        handleSlaPauseResume(ticket, oldStatus, request.getStatus());

        if (request.getStatus() == TicketStatus.RESOLVED && ticket.getResolvedAt() == null)
            ticket.setResolvedAt(Instant.now());
        if (request.getStatus() == TicketStatus.CLOSED && ticket.getClosedAt() == null)
            ticket.setClosedAt(Instant.now());

        ticketRepository.save(ticket);
        createHistoryEntry(ticket, userId, "status", oldStatus.name(), request.getStatus().name(),
                "Status changed from " + oldStatus + " to " + request.getStatus());

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

    // --- Utility + Builder Methods ---

    private void createHistoryEntry(TicketModel ticket, Long userId, String field, String oldValue, String newValue, String desc) {
        TicketHistoryModel history = new TicketHistoryModel();
        history.setTicket(ticket);
        history.setChangedBy(userId);
        history.setFieldName(field);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setChangeDescription(desc);
        historyRepository.save(history);
    }

    private String generateTicketNumber(String projectCode) {
        long count = ticketRepository.count() + 1;
        return projectCode + "-" + count;
    }

    private String generateTicketCode() {
        var zdt = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC);
        return String.format("TCK-%d-%04d", zdt.getYear(), (ticketRepository.count() + 1) % 10000);
    }

    private String computePriorityCode(String impactRaw, String urgencyRaw) {
        if (impactRaw == null || urgencyRaw == null) return "P3";
        String impact = impactRaw.trim().toUpperCase();
        String urgency = urgencyRaw.trim().toUpperCase();
        return switch (impact) {
            case "LOW" -> switch (urgency) {
                case "LOW" -> "P4"; case "MEDIUM", "HIGH" -> "P3"; default -> "P2";
            };
            case "MEDIUM" -> switch (urgency) {
                case "CRITICAL" -> "P1"; case "HIGH" -> "P2"; default -> "P3";
            };
            case "HIGH" -> switch (urgency) {
                case "HIGH", "CRITICAL" -> "P1"; default -> "P2";
            };
            default -> "P3";
        };
    }

    private int[] getSlaHours(String priorityCode) {
        return switch (priorityCode) {
            case "P1" -> new int[]{1, 2};
            case "P2" -> new int[]{2, 4};
            case "P4" -> new int[]{6, 12};
            default -> new int[]{4, 8};
        };
    }

    private com.its.commonservice.enums.TicketPriority mapPriorityEnum(String code) {
        return switch (code) {
            case "P1" -> com.its.commonservice.enums.TicketPriority.CRITICAL;
            case "P2" -> com.its.commonservice.enums.TicketPriority.HIGH;
            case "P4" -> com.its.commonservice.enums.TicketPriority.LOW;
            default -> com.its.commonservice.enums.TicketPriority.MEDIUM;
        };
    }

    private void handleSlaPauseResume(TicketModel ticket, TicketStatus oldStatus, TicketStatus newStatus) {
        boolean shouldPause = newStatus == TicketStatus.ON_HOLD || newStatus == TicketStatus.AWAITING_USER_INFO;
        boolean wasPaused = Boolean.TRUE.equals(ticket.getSlaPaused());
        Instant now = Instant.now();

        if (shouldPause && !wasPaused) {
            if (ticket.getSlaResponseDueAt() != null)
                ticket.setSlaResponseRemainingSeconds(Math.max(0, ticket.getSlaResponseDueAt().getEpochSecond() - now.getEpochSecond()));
            if (ticket.getSlaResolutionDueAt() != null)
                ticket.setSlaResolutionRemainingSeconds(Math.max(0, ticket.getSlaResolutionDueAt().getEpochSecond() - now.getEpochSecond()));
            ticket.setSlaPaused(true);
            ticket.setSlaResponseDueAt(null);
            ticket.setSlaResolutionDueAt(null);
        } else if (!shouldPause && wasPaused) {
            if (ticket.getSlaResponseRemainingSeconds() != null)
                ticket.setSlaResponseDueAt(now.plusSeconds(ticket.getSlaResponseRemainingSeconds()));
            if (ticket.getSlaResolutionRemainingSeconds() != null)
                ticket.setSlaResolutionDueAt(now.plusSeconds(ticket.getSlaResolutionRemainingSeconds()));
            ticket.setSlaPaused(false);
            ticket.setSlaResponseRemainingSeconds(null);
            ticket.setSlaResolutionRemainingSeconds(null);
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

    private String formatAssignment(AssignmentType type, Long userId, Long groupId) {
        if (type == null) return "Unassigned";
        return switch (type) {
            case USER -> "User:" + userId;
            case GROUP -> "Group:" + groupId;
        };
    }

    private TicketDTO buildTicketDTO(TicketModel t) {
        return TicketDTO.builder()
                .id(t.getId())
                .ticketNumber(t.getTicketNumber())
                .ticketCode(t.getTicketCode())
                .organizationId(t.getOrganizationId())
                .projectId(t.getProject().getId())
                .projectName(t.getProject().getName())
                .projectCode(t.getProject().getProjectCode())
                .title(t.getTitle())
                .description(t.getDescription())
                .issueType(t.getIssueType())
                .status(t.getStatus())
                .priority(t.getPriority())
                .impact(t.getImpact())
                .urgency(t.getUrgency())
                .reporterId(t.getReporterId())
                .requestName(t.getRequestName())
                .requestContact(t.getRequestContact())
                .assignmentType(t.getAssignmentType())
                .assignedUserId(t.getAssignedUserId())
                .assignedGroupId(t.getAssignedGroupId())
                .slaType(t.getSlaType())
                .responseSlaHours(t.getResponseSlaHours())
                .resolutionSlaHours(t.getResolutionSlaHours())
                .slaResponseDueAt(t.getSlaResponseDueAt())
                .slaResolutionDueAt(t.getSlaResolutionDueAt())
                .slaPaused(t.getSlaPaused())
                .slaBreached(t.getSlaBreached())
                .createdAt(t.getCreatedAt())
                .updatedAt(t.getUpdatedAt())
                .build();
    }
}
