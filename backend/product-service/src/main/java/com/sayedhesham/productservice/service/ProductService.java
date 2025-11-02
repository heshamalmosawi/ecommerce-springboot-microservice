package com.sayedhesham.productservice.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.sayedhesham.productservice.dto.ProductDTO;
import com.sayedhesham.productservice.model.Product;
import com.sayedhesham.productservice.repository.ProductRepository;
import com.sayedhesham.productservice.repository.UserRepository;

@Service
public class ProductService {

    private final ProductRepository prodRepo;
    private final UserRepository userRepo;

    public ProductService(ProductRepository prodRepository, UserRepository userRepository) {
        this.prodRepo = prodRepository;
        this.userRepo = userRepository;
    }

    public List<Product> getAll() {
        return prodRepo.findAll();
    }

    public Product getById(String id) {
        return prodRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product create(ProductDTO productDTO) {
        // Validate DTO
        if (productDTO.getName() == null || productDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (productDTO.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        if (productDTO.getDescription() == null || productDTO.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Product description is required");
        }
        if (productDTO.getQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity must be non-negative");
        }

        String currentUserId = getCurrentUserId();
        if (!userRepo.existsById(currentUserId)) {
            throw new IllegalArgumentException("User does not exist");
        }

        Product product = Product.builder()
                .name(productDTO.getName())
                .description(productDTO.getDescription())
                .price(productDTO.getPrice())
                .quantity(productDTO.getQuantity())
                .userId(currentUserId)
                .build();

        return prodRepo.save(product);
    }

    public Product replaceProduct(String id, ProductDTO productDTO) {
        Product existingProduct = prodRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        String currentUserId = getCurrentUserId();
        if (!existingProduct.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only modify your own products");
        }
        if (!userRepo.existsById(currentUserId)) {
            throw new IllegalArgumentException("User does not exist");
        }

        // Validate DTO
        if (productDTO.getName() == null || productDTO.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (productDTO.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        if (productDTO.getDescription() == null || productDTO.getDescription().isEmpty()) {
            throw new IllegalArgumentException("Product description is required");
        }
        if (productDTO.getQuantity() < 0) {
            throw new IllegalArgumentException("Product quantity must be non-negative");
        }

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setQuantity(productDTO.getQuantity());
        existingProduct.setUserId(currentUserId);

        return prodRepo.save(existingProduct);
    }

    public Product update(String id, ProductDTO productDTO) {
        Product existingProduct = prodRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        String currentUserId = getCurrentUserId();
        if (!existingProduct.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only modify your own products");
        }

        if (!userRepo.existsById(currentUserId)) {
            throw new IllegalArgumentException("User does not exist");
        }
        if (productDTO.getName() != null && !productDTO.getName().isEmpty()) {
            existingProduct.setName(productDTO.getName());
        }
        if (productDTO.getPrice() > 0) {
            existingProduct.setPrice(productDTO.getPrice());
        }
        if (productDTO.getDescription() != null && !productDTO.getDescription().isEmpty()) {
            existingProduct.setDescription(productDTO.getDescription());
        }
        if (productDTO.getQuantity() >= 0) {
            existingProduct.setQuantity(productDTO.getQuantity());
        }

        return prodRepo.save(existingProduct);
    }

    public void delete(String id) {
        Product existingProduct = prodRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        String currentUserId = getCurrentUserId();
        if (!existingProduct.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only delete your own products");
        }
        prodRepo.delete(existingProduct);
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        throw new IllegalArgumentException("User not authenticated");
    }
}
