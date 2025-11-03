package com.its.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user login
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {
    
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;
    
    @NotBlank(message = "Password is required")
    private String password;
}
