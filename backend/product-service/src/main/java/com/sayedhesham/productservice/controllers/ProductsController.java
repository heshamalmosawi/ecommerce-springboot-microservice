package com.sayedhesham.productservice.controllers;


import java.util.List;

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

    private static final String ERROR_PREFIX = "Error: ";

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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_PREFIX + e.getMessage());
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_PREFIX + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getProductById(@PathVariable String id) {
        try {
            ProductResponseDTO product = prodService.getByIdWithSellerName(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException r) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ERROR_PREFIX + r.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_PREFIX + e.getMessage());

        }
    }

    @PostMapping
    public ResponseEntity<Object> createProduct(@Valid @RequestBody ProductDTO createProductDTO) {
        try {
            Product createdProduct = prodService.create(createProductDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
        } catch (Exception e) {
            System.out.println("Error creating product: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_PREFIX + e.getMessage());
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProductWithImages(@PathVariable String id, @Valid @RequestBody ProductUpdateWithImagesDTO product) {
        try {
            Product updatedProduct = prodService.updateProductWithImages(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException r) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ERROR_PREFIX + r.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_PREFIX + e.getMessage());
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Object> updateProduct(@PathVariable String id, @Valid @RequestBody ProductDTO product) {
        try {
            Product updatedProduct = prodService.update(id, product);
            return ResponseEntity.ok(updatedProduct);
        } catch (RuntimeException r) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ERROR_PREFIX + r.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_PREFIX + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProduct(@PathVariable String id) {
        try {
            prodService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException r) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ERROR_PREFIX + r.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_PREFIX + e.getMessage());
        }
    }

    /**
     * Get product IDs for the current seller (authenticated user)
     * Used by order-service for seller analytics
     * Requires SELLER role
     */
    @GetMapping("/seller/ids")
    public ResponseEntity<Object> getMyProductIds() {
        System.out.println("[ProductsController] /seller/ids endpoint called");
        
        try {
            System.out.println("[ProductsController] Calling ProductService.getMyProductIds");
            List<String> productIds = prodService.getMyProductIds();
            System.out.println("[ProductsController] Successfully retrieved " + productIds.size() + " product IDs");
            return ResponseEntity.ok(productIds);
        } catch (IllegalArgumentException e) {
            System.err.println("[ProductsController] Unauthorized error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ERROR_PREFIX + e.getMessage());
        } catch (Exception e) {
            System.err.println("[ProductsController] Internal server error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ERROR_PREFIX + e.getMessage());
        }
    }

    /**
     * Get products by list of IDs
     * Used by order-service for reorder functionality
     *
     * @param ids List of product IDs
     * @return List of product response DTOs
     */
    @GetMapping("/batch")
    public ResponseEntity<Object> getProductsBatch(@RequestParam List<String> ids) {
        System.out.println("[ProductsController] /batch endpoint called with " + (ids != null ? ids.size() : 0) + " IDs");
        
        // Validate input
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR_PREFIX + "Product IDs list cannot be null or empty");
        }
        
        // Limit batch size to prevent DoS attacks
        final int MAX_BATCH_SIZE = 100;
        if (ids.size() > MAX_BATCH_SIZE) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR_PREFIX + "Batch size cannot exceed " + MAX_BATCH_SIZE + " items");
        }
        
        // Validate individual IDs
        List<String> invalidIds = ids.stream()
            .filter(id -> id == null || id.trim().isEmpty())
            .toList();
        if (!invalidIds.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ERROR_PREFIX + "Product IDs cannot be null or empty strings");
        }
        
        try {
            List<ProductResponseDTO> products = prodService.getProductsByIds(ids);
            System.out.println("[ProductsController] Successfully retrieved " + products.size() + " products");
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            System.err.println("[ProductsController] Error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ERROR_PREFIX + e.getMessage());
        }
    }
}
