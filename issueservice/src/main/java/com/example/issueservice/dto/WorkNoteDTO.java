package com.example.issueservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkNoteDTO {
    private Long id;
    private Long ticketId;
    private Long createdBy;
    private String note;
    private Boolean internal;
    private Instant createdAt;
}
