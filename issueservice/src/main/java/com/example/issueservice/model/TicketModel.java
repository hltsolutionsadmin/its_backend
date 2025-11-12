    package com.example.issueservice.model;

    import com.its.commonservice.enums.*;
    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;

    import java.time.LocalDateTime;
    import java.util.ArrayList;
    import java.util.List;

    @Entity
    @Table(name = "TICKETS", indexes = {
            @Index(name = "idx_ticket_status", columnList = "STATUS"),
            @Index(name = "idx_ticket_priority", columnList = "PRIORITY"),
            @Index(name = "idx_ticket_project", columnList = "PROJECT_ID"),
            @Index(name = "idx_ticket_created_by", columnList = "CREATED_BY_ID")
    })

    @Getter
    @Setter
    public class TicketModel extends GenericModel {

        @Column(name = "TICKET_ID", unique = true, nullable = true, length = 50)
        private String ticketId;

        @Column(name = "TICKET_NUMBER")
        private Long ticketNumber;

        @Column(name = "TITLE", nullable = false, length = 500)
        private String title;

        @Column(name = "DESCRIPTION", length = 4000)
        private String description;

        @Enumerated(EnumType.STRING)
        @Column(name = "PRIORITY", nullable = false)
        private TicketPriority priority;

        @Enumerated(EnumType.STRING)
        @Column(name = "STATUS", nullable = false, length = 50)
        private TicketStatus status;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "PROJECT_ID", nullable = false)
        private ProjectModel project;

        @Column(name = "CREATED_BY_ID")
        private Long createdById;

        @Column(name = "ASSIGNED_TO")
        private Long assignedToId;

        @Column(name = "RESOLVED_AT")
        private LocalDateTime resolvedAt;

        @Column(name = "DUE_DATE")
        private LocalDateTime dueDate;

        @ElementCollection
        @CollectionTable(name = "ticket_comment_ids", joinColumns = @JoinColumn(name = "ticket_id"))
        @Column(name = "comment_id")
        private List<Long> commentIds = new ArrayList<>();


        @Column(name = "IS_ARCHIVED")
        private Boolean archived = false;

        @Column(name = "GROUP_ID")
        private Long groupId;


        @Enumerated(EnumType.STRING)
        @Column(name = "URGENCY", nullable = false)
        private Urgency urgency;

        @Enumerated(EnumType.STRING)
        @Column(name = "ISSUE_TYPE", nullable = false)
        private IssueType issueType;


        @Enumerated(EnumType.STRING)
        @Column(name = "IMPACT", nullable = false)
        private Impact impact;

    }
