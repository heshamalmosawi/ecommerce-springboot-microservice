package com.sayedhesham.productservice.dto;

import com.sayedhesham.productservice.model.Category;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSearchRequest {
    private String name;
    private Double minPrice;
    private Double maxPrice;
    private String sellerName;
    private Category category;
}