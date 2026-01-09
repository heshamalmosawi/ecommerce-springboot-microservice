package com.sayedhesham.orderservice.model;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    private String buyerId;

    private String email;

    private String internationalPhone;

    private String fullName;

    private String address;

    private String city;

    private String postalCode;

    private List<OrderItem> orderItems;

    private Double totalPrice;

    private String status;

    private LocalDateTime orderDate;

    private String cancellationReason;
}
