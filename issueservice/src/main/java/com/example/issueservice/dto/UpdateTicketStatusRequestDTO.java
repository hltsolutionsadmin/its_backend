package com.example.issueservice.dto;

import com.juvarya.commonservice.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketStatusRequestDTO {
    
    @NotNull(message = "Status is required")
    private TicketStatus status;
    
    @Size(max = 1000)
    private String comment;  // Optional comment about status change
}
