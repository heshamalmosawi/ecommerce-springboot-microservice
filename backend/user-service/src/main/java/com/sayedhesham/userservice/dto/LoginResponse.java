package com.sayedhesham.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    @NotBlank
    private String token;

    @NotBlank
    private long expiresAt; // unix timestamp
}