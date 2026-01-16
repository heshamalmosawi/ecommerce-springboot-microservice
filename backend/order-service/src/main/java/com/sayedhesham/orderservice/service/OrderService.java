package com.sayedhesham.orderservice.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.sayedhesham.orderservice.dto.OrderDTO;
import com.sayedhesham.orderservice.dto.OrderItemDTO;
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

    public Page<Order> getMyOrders(String userId, org.springframework.data.domain.Pageable pageable) {
        return orderRepo.findByBuyerId(userId, pageable);
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
}
