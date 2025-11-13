package com.its.userservice.dto;

import com.its.common.dto.UserDTO;
import com.its.commonservice.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for inviting a user to an organization
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InviteUserRequestDTO {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotNull(message = "Role is required")
    private UserRole role;
    private Long departmentId;
    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean active;
    private Boolean emailVerified;
    private String fullName;
    private String primaryContact;
    private String password;
}
