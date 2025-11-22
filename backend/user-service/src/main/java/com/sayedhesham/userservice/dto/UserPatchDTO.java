    package com.sayedhesham.userservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserPatchDTO {

    private String name;
    private String email;
    private String role;
    private String avatarBase64; // Base64 encoded avatar image

}