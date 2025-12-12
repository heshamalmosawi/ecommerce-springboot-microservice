package com.sayedhesham.productservice.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.sayedhesham.productservice.dto.ProductDTO;
import com.sayedhesham.productservice.model.Product;
import com.sayedhesham.productservice.model.User;
import com.sayedhesham.productservice.repository.ProductRepository;
import com.sayedhesham.productservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository prodRepo;

    @Mock
    private UserRepository userRepo;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private User testUser;

    @BeforeEach
    void setUp() {
        testProduct = Product.builder()
                .id("prod1")
                .name("Test Product")
                .description("This is a test product")
                .price(99.99)
                .quantity(10)
                .userId("user1")
                .imageMediaIds(new ArrayList<>())
                .build();

        testUser = User.builder()
                .id("user1")
                .name("Test User")
                .email("abcd@email.com")
                .password("password")
                .role("seller")
                .build();

    }

    @Test
    void getAll_ShouldReturnListOfProducts() {
        when(prodRepo.findAll()).thenReturn(java.util.Arrays.asList(testProduct));

        java.util.List<Product> result = productService.getAll();

        assertEquals(1, result.size());
        verify(prodRepo).findAll();
    }

    @Test
    void getAllWithPageable_ShouldReturnPageOfProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct));
        when(prodRepo.findAll(pageable)).thenReturn(productPage);

        Page<Product> result = productService.getAll(pageable);

        assertEquals(1, result.getContent().size());
        verify(prodRepo).findAll(pageable);
    }

    @Test
    void getById_WhenProductExists_ShouldReturnProduct() {
        when(prodRepo.findById("prod1")).thenReturn(Optional.of(testProduct));

        Product result = productService.getById("prod1");

        assertNotNull(result);
        verify(prodRepo).findById("prod1");
    }

    @Test
    void getById_WhenProductNotExists_ShouldThrowException() {
        when(prodRepo.findById("nonexistent")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> productService.getById("nonexistent"));
        assertEquals("Product not found", exception.getMessage());
    }

    @Test
    void getByIdWithSellerName_WhenProductExists_ShouldReturnProductResponseDTO() {
        when(prodRepo.findById("prod1")).thenReturn(Optional.of(testProduct));
        when(userRepo.findById("user1")).thenReturn(Optional.of(testUser));

        var result = productService.getByIdWithSellerName("prod1");

        assertNotNull(result);
        assertEquals("prod1", result.getId());
        assertEquals("Test Product", result.getName());
        assertEquals("Test User", result.getSellerName());
        verify(prodRepo).findById("prod1");
        verify(userRepo).findById("user1");
    }

    @Test
    void create_ValidProductDTO_ShouldReturnProduct() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("user1");

        var productDTO = ProductDTO.builder()
                .name("New Product")
                .description("New Description")
                .price(50.0)
                .quantity(5)
                .images(new ArrayList<>())
                .build();

        when(userRepo.existsById("user1")).thenReturn(true);
        when(prodRepo.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.create(productDTO);

        assertNotNull(result);
        verify(userRepo).existsById("user1");
        verify(prodRepo).save(any(Product.class));
    }

    @Test
    void update_ValidProductDTO_ShouldReturnUpdatedProduct() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("user1");

        var productDTO = ProductDTO.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(150.0)
                .quantity(15)
                .build();

        when(prodRepo.findById("prod1")).thenReturn(Optional.of(testProduct));
        when(userRepo.existsById("user1")).thenReturn(true);
        when(prodRepo.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.update("prod1", productDTO);

        assertNotNull(result);
        verify(prodRepo).findById("prod1");
        verify(prodRepo).save(any(Product.class));
    }

    @Test
    void delete_ProductExists_ShouldDeleteProduct() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUsername()).thenReturn("user1");

        when(prodRepo.findById("prod1")).thenReturn(Optional.of(testProduct));
        doNothing().when(prodRepo).delete(testProduct);

        productService.delete("prod1");

        verify(prodRepo).findById("prod1");
        verify(prodRepo).delete(testProduct);
    }
}
