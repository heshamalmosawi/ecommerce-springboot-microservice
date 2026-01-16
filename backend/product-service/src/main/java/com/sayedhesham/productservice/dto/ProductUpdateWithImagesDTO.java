package com.sayedhesham.productservice.dto;

import java.util.List;

import com.sayedhesham.productservice.model.Category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * DTO for product updates with image operations.
 * Used specifically for enhanced PUT operations to handle
 * new images and retained existing images efficiently.
 */
@Data
public class ProductUpdateWithImagesDTO {
    @NotBlank private String name;
    @NotBlank private String description;
    @NotNull @Positive private Double price;
    @NotNull @Positive private Integer quantity = 1; // Default quantity, must be positive
    @NotNull private Category category;

    private List<String> images; // Base64 encoded new images to add
    private List<String> retainedImageIds; // Existing image IDs to keep
}