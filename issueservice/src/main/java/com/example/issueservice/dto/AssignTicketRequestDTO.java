package com.example.issueservice.dto;

import com.juvarya.commonservice.enums.AssignmentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignTicketRequestDTO {
    
    @NotNull(message = "Assignment type is required")
    private AssignmentType assignmentType;  // USER or GROUP
    
    private Long userId;  // Required if assignmentType = USER
    
    private Long groupId;  // Required if assignmentType = GROUP
}
