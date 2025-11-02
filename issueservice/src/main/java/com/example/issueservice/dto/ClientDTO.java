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
public class ClientDTO {
    
    private Long id;
    private Long organizationId;
    private String name;
    private String email;
    private String phone;
    private String company;
    private Boolean active;
    private Instant createdAt;
}
