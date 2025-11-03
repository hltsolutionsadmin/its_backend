package com.example.issueservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Group change history for tickets
 */
@Entity
@Table(name = "group_history", indexes = {
    @Index(name = "idx_ticket_group_hist", columnList = "ticket_id,changedAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupHistoryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketModel ticket;

    @Column
    private Long fromGroupId;

    @Column
    private Long toGroupId;

    @Column(nullable = false)
    private Long changedBy;

    @Column(length = 1000)
    private String note;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant changedAt;
}
