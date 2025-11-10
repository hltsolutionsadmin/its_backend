package com.its.common.dto;

import com.its.commonservice.enums.AssignmentType;
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
    
    // Optional note to record in history/group history
    private String note;
}
