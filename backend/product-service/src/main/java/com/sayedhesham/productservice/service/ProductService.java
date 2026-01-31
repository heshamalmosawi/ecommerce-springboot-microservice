package com.sayedhesham.productservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.sayedhesham.productservice.dto.ProductDTO;
import com.sayedhesham.productservice.dto.ProductResponseDTO;
import com.sayedhesham.productservice.dto.ProductSearchRequest;
import com.sayedhesham.productservice.dto.ProductUpdateWithImagesDTO;
import com.sayedhesham.productservice.model.Category;
import com.sayedhesham.productservice.model.Product;
import com.sayedhesham.productservice.model.User;
import com.sayedhesham.productservice.repository.ProductRepository;
import com.sayedhesham.productservice.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductService {

    private static final String PRODUCT_NOT_FOUND = "Product not found";

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

    public Page<ProductResponseDTO> getAll(Pageable pageable) {
        return prodRepo.findAll(pageable).map(this::convertToProductResponseDTO);
    }

    public Page<ProductResponseDTO> searchProducts(ProductSearchRequest searchRequest, Pageable pageable) {
        String name = searchRequest.getName();
        Double minPrice = searchRequest.getMinPrice();
        Double maxPrice = searchRequest.getMaxPrice();
        String sellerName = searchRequest.getSellerName();
        Category category = searchRequest.getCategory();

        List<String> userIds = null;
        if (sellerName != null && !sellerName.trim().isEmpty()) {
            userIds = userRepo.findByNameContainingIgnoreCase(sellerName)
                    .stream()
                    .map(User::getId)
                    .toList();
        }

        Page<Product> products = prodRepo.searchProducts(name, minPrice, maxPrice, userIds, category, pageable);
        return products.map(this::convertToProductResponseDTO);
    }

    private ProductResponseDTO convertToProductResponseDTO(Product product) {
        User seller = userRepo.findById(product.getUserId())
                .orElse(null);

        String sellerName = seller != null ? seller.getName() : "Unknown Seller";

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .sellerName(sellerName)
                .category(product.getCategory())
                .categoryDisplayName(product.getCategory() != null ? product.getCategory().toDisplayName() : "Other")
                .imageMediaIds(product.getImageMediaIds())
                .build();
    }

    public Product getById(String id) {
        return prodRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));
    }

    public ProductResponseDTO getByIdWithSellerName(String id) {
        Product product = prodRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(PRODUCT_NOT_FOUND));

        User seller = userRepo.findById(product.getUserId())
                .orElseThrow(() -> new RuntimeException("Seller not found"));

        ProductResponseDTO responseDTO = ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .sellerName(seller.getName())
                .category(product.getCategory())
                .categoryDisplayName(product.getCategory() != null ? product.getCategory().toDisplayName() : "Other")
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
        if (productDTO.getCategory() == null) {
            throw new IllegalArgumentException("Product category is required");
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
                .category(productDTO.getCategory())
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
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
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
        if (productDTO.getCategory() == null) {
            throw new IllegalArgumentException("Product category is required");
        }

        existingProduct.setName(productDTO.getName());
        existingProduct.setDescription(productDTO.getDescription());
        existingProduct.setPrice(productDTO.getPrice());
        existingProduct.setQuantity(productDTO.getQuantity());
        existingProduct.setCategory(productDTO.getCategory());
        existingProduct.setUserId(currentUserId);

        return prodRepo.save(existingProduct);
    }

    public Product update(String id, ProductDTO productDTO) {
        Product existingProduct = prodRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
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
        if (productDTO.getCategory() != null) {
            existingProduct.setCategory(productDTO.getCategory());
        }

        return prodRepo.save(existingProduct);
    }

    public void delete(String id) {
        Product existingProduct = prodRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
        String currentUserId = getCurrentUserId();
        if (!existingProduct.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only delete your own products");
        }
        prodRepo.delete(existingProduct);
    }

    public Product updateProductWithImages(String id, ProductUpdateWithImagesDTO productDTO) {
        Product existingProduct = prodRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(PRODUCT_NOT_FOUND));
        
        validateProductOwnership(existingProduct);
        validateProductFields(productDTO);
        updateProductFields(existingProduct, productDTO);
        handleImageUpdates(id, existingProduct, productDTO);

        return prodRepo.save(existingProduct);
    }

    private void validateProductOwnership(Product product) {
        String currentUserId = getCurrentUserId();
        if (!product.getUserId().equals(currentUserId)) {
            throw new IllegalArgumentException("You can only modify your own products");
        }
        if (!userRepo.existsById(currentUserId)) {
            throw new IllegalArgumentException("User does not exist");
        }
    }

    private void validateProductFields(ProductUpdateWithImagesDTO productDTO) {
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
        if (productDTO.getCategory() == null) {
            throw new IllegalArgumentException("Product category is required");
        }
    }

    private void updateProductFields(Product product, ProductUpdateWithImagesDTO productDTO) {
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setQuantity(productDTO.getQuantity());
        product.setCategory(productDTO.getCategory());
    }

    private void handleImageUpdates(String productId, Product product, ProductUpdateWithImagesDTO productDTO) {
        List<String> currentImageIds = product.getImageMediaIds() != null 
                ? product.getImageMediaIds() : new ArrayList<>();
        List<String> retainedIds = productDTO.getRetainedImageIds() != null 
                ? productDTO.getRetainedImageIds() : new ArrayList<>();

        validateRetainedImageIds(currentImageIds, retainedIds);
        publishImageDeleteEvents(productId, currentImageIds, retainedIds);
        publishImageUploadEvents(productId, productDTO.getImages());
        
        product.setImageMediaIds(new ArrayList<>(retainedIds));
    }

    private void validateRetainedImageIds(List<String> currentImageIds, List<String> retainedIds) {
        for (String retainedId : retainedIds) {
            if (!currentImageIds.contains(retainedId)) {
                throw new IllegalArgumentException("Image ID " + retainedId + " does not belong to this product");
            }
        }
    }

    private void publishImageDeleteEvents(String productId, List<String> currentImageIds, List<String> retainedIds) {
        List<String> imagesToRemove = currentImageIds.stream()
                .filter(imageId -> !retainedIds.contains(imageId))
                .toList();

        for (String imageId : imagesToRemove) {
            productImageEventService.publishProductImageDeleteEvent(productId, imageId);
        }
    }

    private void publishImageUploadEvents(String productId, List<String> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (String imageBase64 : images) {
            String contentType = extractContentType(imageBase64);
            productImageEventService.publishProductImageUploadEvent(productId, imageBase64, contentType);
        }
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

    /**
     * Get all product IDs for the current seller (authenticated user)
     *
     * @return List of product IDs
     */
    public List<String> getMyProductIds() {
        log.debug("getMyProductIds called");
        
        try {
            String currentUserId = getCurrentUserId();
            log.debug("Current user ID: {}", currentUserId);
            
            List<Product> products = prodRepo.findProductIdsByUserId(currentUserId);
            log.debug("Found {} products for user", products.size());
            
            List<String> productIds = products.stream()
                    .map(Product::getId)
                    .toList();
            
            log.debug("Returning product IDs: {}", productIds);
            return productIds;
        } catch (Exception e) {
            log.error("Error in getMyProductIds: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Get products by list of IDs
     *
     * @param ids List of product IDs
     * @return List of product response DTOs
     */
    public List<ProductResponseDTO> getProductsByIds(List<String> ids) {
        log.debug("getProductsByIds called with {} IDs", ids.size());
        
        List<Product> products = prodRepo.findAllById(ids);
        log.debug("Found {} products", products.size());
        
        return products.stream()
                .map(this::convertToProductResponseDTO)
                .collect(Collectors.toList());
    }
}
