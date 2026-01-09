package com.sayedhesham.orderservice.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {

    @NotBlank
    @Email
    String email;

    @NotBlank
    @Pattern(regexp = "^\\+[1-9]\\d{1,14}$")
    String internationalPhone;  // E.164

    @NotBlank @Length(max = 100)
    String fullName;

    @NotBlank
    String address;
    
    @NotBlank
    String city;
    
    @NotBlank
    @Pattern(regexp = "\\d{4,10}")
    String postalCode;  // Flexible

    @NotNull
    OrderItemDTO[] orderItems;
}
