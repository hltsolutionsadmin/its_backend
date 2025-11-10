package com.its.common.dto;

import com.its.commonservice.enums.GroupLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupDTO {
    
    private Long id;
    private Long organizationId;
    private String name;
    private String description;
    private GroupLevel level;
    private Boolean active;
    private Instant createdAt;
    private Integer memberCount;
}
