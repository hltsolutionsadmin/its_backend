package com.example.issueservice.model;

import com.juvarya.commonservice.enums.CommentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Comment/Work Note entity for tickets
 */
@Entity
@Table(name = "comments", indexes = {
    @Index(name = "idx_ticket_comment", columnList = "ticket_id,createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketModel ticket;
    
    @Column(nullable = false)
    private Long authorId;  // User who created the comment
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentType type = CommentType.COMMENT;  // COMMENT, WORK_NOTE, SYSTEM
    
    @Column(nullable = false)
    private Boolean isInternal = false;  // Work notes are internal
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
