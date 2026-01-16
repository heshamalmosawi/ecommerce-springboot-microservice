package com.sayedhesham.productservice.controllers;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sayedhesham.productservice.dto.ProductDTO;
import com.sayedhesham.productservice.dto.ProductResponseDTO;
import com.sayedhesham.productservice.dto.ProductSearchRequest;
import com.sayedhesham.productservice.dto.ProductUpdateWithImagesDTO;
import com.sayedhesham.productservice.model.Category;
import com.sayedhesham.productservice.model.Product;
import com.sayedhesham.productservice.service.ProductService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/")
public class ProductsController {

    @Autowired
    private ProductService prodService;

    @GetMapping
    public ResponseEntity<Object> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
            Page<ProductResponseDTO> productPage = prodService.getAll(pageable);
            return ResponseEntity.ok(productPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sellerName,
            @RequestParam(required = false) Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        try {
            if (minPrice != null && minPrice < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("minPrice cannot be negative");
            }
            if (maxPrice != null && maxPrice < 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("maxPrice cannot be negative");
            }
            if (minPrice != null && maxPrice != null && minPrice > maxPrice) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("minPrice cannot be greater than maxPrice");
            }

            Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

            ProductSearchRequest searchRequest = ProductSearchRequest.builder()
                    .name(name)
                    .minPrice(minPrice)
                    .maxPrice(maxPrice)
                    .sellerName(sellerName)
                    .category(category)
                    .build();
            var result = prodService.searchProducts(searchRequest, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable String id) {
        try {
            ProductResponseDTO product = prodService.getByIdWithSellerName(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException r) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + r.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());

        }
    }

    @PostMapping
    public ResponseEntity<Object> createProduct(@Valid @RequestBody ProductDTO createProductDTO) {
        try {
            Product createdProduct = prodService.create(createProductDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (Exception e) {
            System.out.println("Error creating product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProductWithImages(@PathVariable String id, @Valid @RequestBody ProductUpdateWithImagesDTO product) {
        try {
            Product updatedProduct = prodService.updateProductWithImages(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException r) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + r.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable String id, @Valid @RequestBody ProductDTO product) {
        try {
            Product updatedProduct = prodService.update(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException r) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + r.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable String id) {
        try {
            prodService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException r) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + r.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: " + e.getMessage());
        }
    }
}
