package com.sayedhesham.productservice.dto;

import java.util.List;

import com.sayedhesham.productservice.model.Category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponseDTO {
    private String id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private String sellerName;
    private Category category;
    private String categoryDisplayName;
    private List<String> imageMediaIds;
}