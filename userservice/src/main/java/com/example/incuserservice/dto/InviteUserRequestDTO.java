package com.example.incuserservice.dto;

import com.juvarya.commonservice.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
