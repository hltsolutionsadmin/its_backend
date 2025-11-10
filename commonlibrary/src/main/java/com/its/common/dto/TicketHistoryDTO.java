package com.its.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketHistoryDTO {
    
    private Long id;
    private Long ticketId;
    private Long changedBy;
    private String changedByName;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String changeDescription;
    private Instant createdAt;
}
