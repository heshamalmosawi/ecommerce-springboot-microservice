package com.sayedhesham.productservice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.sayedhesham.productservice.model.Category;
import com.sayedhesham.productservice.model.Product;

import java.util.List;

public interface ProductRepositoryCustom {
    Page<Product> searchProducts(String name, Double minPrice, Double maxPrice, List<String> userIds, Category category, Pageable pageable);
}
