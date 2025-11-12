package com.its.common.dto;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {

    @NotNull(message = "fullName must not be null")
    private String fullName;

    @NotNull(message = "email must not be null")
    private String email;

    private Long businessId;

}
