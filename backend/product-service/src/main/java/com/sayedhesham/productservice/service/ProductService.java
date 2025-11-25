package com.sayedhesham.productservice.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.sayedhesham.productservice.dto.ProductDTO;
import com.sayedhesham.productservice.dto.ProductResponseDTO;
import com.sayedhesham.productservice.dto.ProductUpdateWithImagesDTO;
import com.sayedhesham.productservice.model.Product;
import com.sayedhesham.productservice.model.User;
import com.sayedhesham.productservice.repository.ProductRepository;
import com.sayedhesham.productservice.repository.UserRepository;

@Service
public class ProductService {

    private final ProductRepository prodRepo;
    private final UserRepository userRepo;
    private final ProductImageEventService productImageEventService;

    public ProductService(ProductRepository prodRepository, UserRepository userRepository, ProductImageEventService productImageEventService) {
        this.prodRepo = prodRepository;
        this.userRepo = userRepository;
        this.productImageEventService = productImageEventService;
    }

    public List<Product> getAll() {
        return prodRepo.findAll();
    }

    public Product getById(String id) {
        return prodRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public ProductResponseDTO getByIdWithSellerName(String id) {
        Product product = prodRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        
        User seller = userRepo.findById(product.getUserId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));
        
        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .quantity(product.getQuantity())
            .sellerName(seller.getName())
            .imageMediaIds(product.getImageMediaIds())
            .build();
        
        return responseDTO;
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

        // Create product first without images
        Product product = Product.builder()
            .name(productDTO.getName())
            .description(productDTO.getDescription())
            .price(productDTO.getPrice())
            .quantity(productDTO.getQuantity())
            .userId(currentUserId)
            .imageMediaIds(new ArrayList<>())
            .build();
        
        Product savedProduct = prodRepo.save(product);
        
        // Publish image upload events via Kafka (asynchronous)
        if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
            for (String imageBase64 : productDTO.getImages()) {
                String contentType = extractContentType(imageBase64);
                productImageEventService.publishProductImageUploadEvent(
                    savedProduct.getId(), imageBase64, contentType);
            }
        }
        
        return savedProduct;
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

    public Product updateProductWithImages(String id, ProductUpdateWithImagesDTO productDTO) {
        Product existingProduct = prodRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        String currentUserId = getCurrentUserId();
        if (!existingProduct.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only modify your own products");
        }

        if (!userRepo.existsById(currentUserId)) {
            throw new IllegalArgumentException("User does not exist");
        }

        // Validate basic product fields
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

        // Update basic product fields
        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setQuantity(productDTO.getQuantity());

        // Handle image operations
        List<String> currentImageIds = existingProduct.getImageMediaIds();
        List<String> retainedIds = productDTO.getRetainedImageIds();
        
        // Initialize lists if null
        if (currentImageIds == null) {
            currentImageIds = new ArrayList<>();
        }
        if (retainedIds == null) {
            retainedIds = new ArrayList<>();
        }

        // Find images to remove (current images not in retained list)
        final List<String> finalRetainedIds = retainedIds;
        List<String> imagesToRemove = currentImageIds.stream()
                .filter(imageId -> !finalRetainedIds.contains(imageId))
                .toList();

        // Publish delete events for removed images
        for (String imageId : imagesToRemove) {
            productImageEventService.publishProductImageDeleteEvent(id, imageId);
        }

        // Publish upload events for new images
        if (productDTO.getImages() != null && !productDTO.getImages().isEmpty()) {
            for (String imageBase64 : productDTO.getImages()) {
                String contentType = extractContentType(imageBase64);
                productImageEventService.publishProductImageUploadEvent(
                    id, imageBase64, contentType);
            }
        }

        // Update product's image list with retained images
        // Note: New images will be added asynchronously via Kafka events
        existingProduct.setImageMediaIds(new ArrayList<>(retainedIds));

        return prodRepo.save(existingProduct);
    }

    private String extractContentType(String imageBase64) {
        if (imageBase64.contains(",")) {
            String dataUrl = imageBase64.split(",")[0];
            int imageIndex = dataUrl.indexOf("image/");
            int semicolonIndex = dataUrl.indexOf(";");
            // Ensure 'image/' appears before the semicolon and the substring is not empty
            if (imageIndex != -1 && semicolonIndex != -1 && imageIndex < semicolonIndex) {
                return dataUrl.substring(imageIndex, semicolonIndex);
            }
        }
        return "image/jpeg"; // default
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
