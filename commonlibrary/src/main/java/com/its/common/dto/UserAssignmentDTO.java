package com.its.common.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAssignmentDTO {

    private Long id;

    private List<Long> userIds = new ArrayList<>();

    @NotNull(message = "Target ID is required")
    private Long targetId;

    private List<Long> groupIds = new ArrayList<>();

    private Boolean active;

    private String username;
    private String fullName;
    private String primaryContact;
    private String password;
    private String email;
}
