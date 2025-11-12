package com.its.common.dto;

import com.its.commonservice.enums.*;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDTO {

    private Long id;


    private String ticketId;


    private String title;

    private String description;

    private TicketPriority priority;

    private TicketStatus status;

    private Long projectId;

    private Long createdById;

    private String createdByName;

    private Long assignedToId;
    private String assignedToName;

    private LocalDateTime resolvedAt;

    private LocalDateTime dueDate;

    private Boolean archived = false;

    private List<TicketCommentDTO> comments;

    private UserGroupDTO userGroupDTO;

    private Long ticketNumber;
    private Urgency urgency;
    private IssueType issueType;
    private Impact impact;
    private Long groupId;

}
