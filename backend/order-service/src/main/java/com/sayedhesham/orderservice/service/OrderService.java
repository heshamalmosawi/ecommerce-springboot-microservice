package com.sayedhesham.orderservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sayedhesham.orderservice.client.ProductClient;
import com.sayedhesham.orderservice.dto.DateRangeDTO;
import com.sayedhesham.orderservice.dto.OrderDTO;
import com.sayedhesham.orderservice.dto.OrderItemDTO;
import com.sayedhesham.orderservice.dto.PurchaseSummaryDTO;
import com.sayedhesham.orderservice.dto.SellerAnalyticsSummaryDTO;
import com.sayedhesham.orderservice.model.Order;
import com.sayedhesham.orderservice.model.OrderItem;
import com.sayedhesham.orderservice.model.Product;
import com.sayedhesham.orderservice.repository.OrderRepository;
import com.sayedhesham.orderservice.repository.ProductRepository;

@Service
public class OrderService {

    @Autowired
    private ProductRepository prodRepo;

    @Autowired
    private OrderRepository orderRepo;
    
    @Autowired
    private ProductClient productClient;

    public Order create(OrderDTO orderDTO) {
        System.out.println("OrderService: Starting order creation");
        String userId = Utils.getCurrentUserId();
        System.out.println("OrderService: User ID - " + userId);

        // Build order items with product details
        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0.0;

        for (OrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            System.out.println("OrderService: Processing order item");
            // Fetch product details
            Product product = prodRepo.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(
                    "Product not found: " + itemDTO.getProductId()));

            System.out.println("OrderService: Product found");

            // Validate stock availability
            if (product.getQuantity() < itemDTO.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for product: " + product.getName());
            }

            // Create order item with full details
            OrderItem orderItem = OrderItem.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .quantity(itemDTO.getQuantity())
                    .price(product.getPrice())
                    .build();

            orderItems.add(orderItem);
            totalPrice += product.getPrice() * itemDTO.getQuantity();
        }

        // Create and save order
        System.out.println("OrderService: Creating order");
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .buyerId(userId)
                .email(orderDTO.getEmail())
                .internationalPhone(orderDTO.getInternationalPhone())
                .fullName(orderDTO.getFullName())
                .address(orderDTO.getAddress())
                .city(orderDTO.getCity())
                .postalCode(orderDTO.getPostalCode())
                .orderItems(orderItems)
                .totalPrice(totalPrice)
                .status(Order.OrderStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();

        System.out.println("OrderService: Saving order");
        return orderRepo.save(order);
    }

    public void updateOrderStatus(String orderId, Order.OrderStatus status) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        
        order.setStatus(status);
        orderRepo.save(order);
    }

    public Page<Order> getMyOrders(
            String userId, 
            org.springframework.data.domain.Pageable pageable,
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        return orderRepo.findByFilters(userId, status, startDate, endDate, pageable);
    }

    public Order getOrderById(String orderId) {
        String userId = Utils.getCurrentUserId();
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found: " + orderId));
        
        if (!order.getBuyerId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to view this order");
        }
        
        return order;
    }
    
    public PurchaseSummaryDTO getPurchaseAnalytics(
            String userId,
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        PurchaseSummaryDTO summary = orderRepo.getPurchaseAnalytics(
            userId, status, startDate, endDate);
        
        // Add date range to response
        if (startDate != null || endDate != null) {
            summary.setDateRange(DateRangeDTO.builder()
                .start(startDate != null ? startDate.toLocalDate().toString() : null)
                .end(endDate != null ? endDate.toLocalDate().toString() : null)
                .build());
        }
        
        return summary;
    }
    
    /**
     * Get seller analytics by retrieving product IDs from product-service
     * and aggregating order data. Uses automatic token propagation via Feign interceptor.
     * @param status Order status filter (optional)
     * @param startDate Start date for filtering (optional)
     * @param endDate End date for filtering (optional)
     * @return Seller analytics summary
     */
    public SellerAnalyticsSummaryDTO getSellerAnalytics(
            Order.OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        System.out.println("[OrderService] Starting getSellerAnalytics");
        System.out.println("[OrderService] Filters - Status: " + status + ", StartDate: " + startDate + ", EndDate: " + endDate);
        
        // Get seller's product IDs from product-service (token propagated automatically)
        System.out.println("[OrderService] Calling product-service to get seller's product IDs");
        List<String> productIds = productClient.getSellerProductIds();
        System.out.println("[OrderService] Retrieved " + productIds.size() + " product IDs from product-service");
        
        // Get analytics from repository
        System.out.println("[OrderService] Querying order repository for analytics");
        SellerAnalyticsSummaryDTO summary = orderRepo.getSellerAnalytics(
            productIds, status, startDate, endDate);
        System.out.println("[OrderService] Analytics retrieved successfully");
        
        // Add date range to response
        if (startDate != null || endDate != null) {
            summary.setDateRange(DateRangeDTO.builder()
                .start(startDate != null ? startDate.toLocalDate().toString() : null)
                .end(endDate != null ? endDate.toLocalDate().toString() : null)
                .build());
        }
        
        System.out.println("[OrderService] Returning seller analytics summary");
        return summary;
    }
}
