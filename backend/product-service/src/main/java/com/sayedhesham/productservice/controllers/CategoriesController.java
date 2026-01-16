package com.sayedhesham.productservice.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sayedhesham.productservice.model.Category;

@RestController
@RequestMapping("/categories")
public class CategoriesController {

    @GetMapping
    public ResponseEntity<List<Map<String, String>>> getAllCategories() {
        List<Map<String, String>> categories = Arrays.stream(Category.values())
                .map(category -> Map.of(
                        "name", category.name(),
                        "displayName", category.toDisplayName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
}
