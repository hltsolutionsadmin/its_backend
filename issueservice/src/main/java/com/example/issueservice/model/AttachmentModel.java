package com.example.issueservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Attachment entity for tickets
 */
@Entity
@Table(name = "attachments", indexes = {
    @Index(name = "idx_ticket_attachment", columnList = "ticket_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentModel {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketModel ticket;
    
    @Column(nullable = false, length = 500)
    private String fileName;
    
    @Column(nullable = false, length = 1000)
    private String filePath;  // Storage path or URL
    
    @Column(nullable = false, length = 100)
    private String contentType;
    
    @Column(nullable = false)
    private Long fileSize;  // Size in bytes
    
    @Column(nullable = false)
    private Long uploadedBy;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant uploadedAt;
}
