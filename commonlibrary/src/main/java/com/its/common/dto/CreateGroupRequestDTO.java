package com.its.common.dto;

import com.its.commonservice.enums.GroupLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequestDTO {
    
    @NotBlank(message = "Group name is required")
    @Size(min = 2, max = 200)
    private String name;
    
    @Size(max = 1000)
    private String description;
    
    @NotNull(message = "Group level is required")
    private GroupLevel level;  // L1, L2, L3
}
