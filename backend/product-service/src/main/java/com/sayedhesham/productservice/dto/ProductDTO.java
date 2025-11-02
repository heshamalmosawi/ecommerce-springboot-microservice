package com.sayedhesham.productservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductDTO {
    @NotBlank private String name;
    @NotBlank private String description;
    @NotNull private Double price;
    @NotNull
    @Positive
    private Integer quantity = 1; // Default quantity, must be positive
}