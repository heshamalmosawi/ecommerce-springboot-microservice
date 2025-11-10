package com.sayedhesham.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {

    @NotBlank
    private String name;
    @NotBlank
    private String email;
    @NotBlank
    private String password;
    @NotBlank
    private String role;
    private String avatarMediaId; // Reference to Media collection
    private String avatarBase64; // Base64 encoded avatar image

}
