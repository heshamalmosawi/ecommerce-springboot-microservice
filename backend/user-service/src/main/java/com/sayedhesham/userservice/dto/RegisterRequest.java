package com.sayedhesham.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email is required")
    @NotBlank
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank
    private String role; // 'client' or 'sellers'

    private String avatar_b64; // Optional
}
