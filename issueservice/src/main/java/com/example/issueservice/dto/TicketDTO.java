package com.example.issueservice.dto;

import com.its.commonservice.enums.AssignmentType;
import com.its.commonservice.enums.TicketPriority;
import com.its.commonservice.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketDTO {
    
    private Long id;
    private String ticketNumber;
    private String ticketCode;
    private Long organizationId;
    private Long projectId;
    private String projectName;
    private String projectCode;
    private String title;
    private String description;
    private String issueType;
    private TicketStatus status;
    private TicketPriority priority;
    private String priorityLevel;
    private String impact;
    private String urgency;
    private Long reporterId;
    private String reporterName;
    private String requestName;
    private String requestContact;
    private Long clientId;
    private String clientName;
    private Long categoryId;
    private String categoryName;
    private Long subCategoryId;
    private String subCategoryName;
    private AssignmentType assignmentType;
    private Long assignedUserId;
    private String assignedUserName;
    private Long assignedGroupId;
    private String assignedGroupName;
    private Long assetId;
    private Instant dueDate;
    private Instant resolvedAt;
    private Instant closedAt;
    private String slaType;
    private Integer responseSlaHours;
    private Integer resolutionSlaHours;
    private Instant slaResponseDueAt;
    private Instant slaResolutionDueAt;
    private Boolean slaBreached;
    private Instant slaBreachedAt;
    private Boolean slaPaused;
    private Instant createdAt;
    private Instant updatedAt;
    private Integer commentCount;
    private Integer attachmentCount;
}
