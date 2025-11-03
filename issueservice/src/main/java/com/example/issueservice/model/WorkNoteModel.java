package com.example.issueservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Internal work notes vs public comments
 */
@Entity
@Table(name = "work_notes", indexes = {
        @Index(name = "idx_work_note_ticket", columnList = "ticket_id,createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkNoteModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private TicketModel ticket;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String note;

    @Column(nullable = false)
    private Long createdBy;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Boolean internalNote = true; // true: internal work note, false: public comment
}
