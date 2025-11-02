package com.example.issueservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Ticket history entity - tracks all changes
 */
@Entity
@Table(name = "ticket_history", indexes = {
    @Index(name = "idx_ticket_history", columnList = "ticket_id,createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketHistoryModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketModel ticket;
    
    @Column(nullable = false)
    private Long changedBy;  // User who made the change
    
    @Column(nullable = false, length = 100)
    private String fieldName;  // e.g., "status", "assignedUserId", "priority"
    
    @Column(columnDefinition = "TEXT")
    private String oldValue;
    
    @Column(columnDefinition = "TEXT")
    private String newValue;
    
    @Column(columnDefinition = "TEXT")
    private String changeDescription;  // Human-readable description
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
