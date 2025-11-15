package com.sayedhesham.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {

    @NotBlank
    private String name;

    @NotBlank
    private String token;

    @NotNull
    private Long expiresAt; // unix timestamp

}
