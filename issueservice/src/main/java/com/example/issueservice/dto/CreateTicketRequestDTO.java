package com.example.issueservice.dto;

import com.its.commonservice.enums.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequestDTO {
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 500)
    private String title;
    
    @Size(max = 5000)
    private String description;
    
    @NotNull(message = "Priority is required")
    private TicketPriority priority;
    
    @Size(max = 200)
    private String requestName;
    
    @Size(max = 200)
    private String requestContact;
    
    private Long categoryId;
    
    private Long subCategoryId;
    
    private Long clientId;
    
    private Long assetId;
}
