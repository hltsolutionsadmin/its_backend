package com.example.issueservice.model;

import com.its.commonservice.enums.AssignmentType;
import com.its.commonservice.enums.TicketPriority;
import com.its.commonservice.enums.TicketStatus;
import com.its.commonservice.enums.Impact;
import com.its.commonservice.enums.Urgency;
import com.its.commonservice.enums.IssueType;
import com.its.commonservice.enums.SlaType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Ticket entity - core issue/service request
 */
@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_ticket_number", columnList = "ticketNumber", unique = true),
    @Index(name = "idx_ticket_code", columnList = "ticketCode", unique = true),
    @Index(name = "idx_org_ticket", columnList = "organizationId,ticketNumber"),
    @Index(name = "idx_project_ticket", columnList = "project_id"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_assigned_user", columnList = "assignedUserId"),
    @Index(name = "idx_assigned_group", columnList = "assignedGroupId")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String ticketNumber;  // e.g., "PROJ-1234"

    @Column(nullable = false, unique = true, length = 50)
    private String ticketCode;
    
    @Column(nullable = false)
    private Long organizationId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectModel project;
    
    @Column(nullable = false, length = 500)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private IssueType issueType; // Bug, Incident, Service Request, Change
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketStatus status = TicketStatus.NEW;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.MEDIUM; // Legacy field
    
    // New priority level derived from impact x urgency matrix: P1..P4
    @Column(length = 5)
    private String priorityLevel;
    
    // Impact and urgency used to compute priority level
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Impact impact;   // Low, Medium, High
    
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Urgency urgency;  // Low, Medium, High, Critical
    @Column(nullable = false)
    private Long reporterId;  // User who created the ticket
    
    @Column(length = 200)
    private String requestName;  // Name of requester (can be different from reporter)
    
    @Column(length = 200)
    private String requestContact;  // Email/Phone of requester

    @Column(length = 4)
    private String priorityCode; // P1..P4 computed from matrix

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private SlaType slaType; // Standard, Enterprise, 24x7

    @Column
    private Integer responseSlaHours;

    @Column
    private Integer resolutionSlaHours;

    @Column
    private Instant slaResponseDueAt;

    @Column
    private Instant slaResolutionDueAt;

    @Column
    private Boolean slaBreached = false;

    @Column
    private Boolean slaPaused = false;

    @Column
    private Instant slaBreachedAt;

    @Column
    private Long slaResponseRemainingSeconds; // used when paused

    @Column
    private Long slaResolutionRemainingSeconds; // used when paused
    
    @Column
    private Long clientId;  // Reference to ClientModel if created by client
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryModel category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_category_id")
    private SubCategoryModel subCategory;
    
    // Assignment fields
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AssignmentType assignmentType;  // USER or GROUP
    
    @Column
    private Long assignedUserId;
    
    @Column
    private Long assignedGroupId;
    
    @Column
    private Long assetId;  // Optional asset reference
    
    @Column
    private Instant dueDate; // Legacy due field
    @Column
    private Instant resolvedAt;
    @Column
    private Instant closedAt;
    
    // Remaining time in seconds when paused
    private Long slaRemainingResponseSeconds;
    private Long slaRemainingResolutionSeconds;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<CommentModel> comments = new ArrayList<>();
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<TicketHistoryModel> history = new ArrayList<>();
    
    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<AttachmentModel> attachments = new ArrayList<>();
}
