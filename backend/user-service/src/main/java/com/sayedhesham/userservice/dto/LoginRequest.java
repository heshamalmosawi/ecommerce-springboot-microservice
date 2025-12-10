package com.sayedhesham.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
public class LoginRequest {
    @Email
    private String email;

    @NotBlank
    private String password;
}