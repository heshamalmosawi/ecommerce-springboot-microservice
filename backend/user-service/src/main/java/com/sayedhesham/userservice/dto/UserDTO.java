package com.sayedhesham.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {

    @NotBlank
    private String name;
    @NotBlank
    private String email;
    @NotBlank
    private String role;
    private String avatarMediaId; // Reference to Media collection
    private String avatarBase64; // Base64 encoded avatar image

}
