# Order Service

The Order Service is a microservice responsible for managing order lifecycle, order processing, and order analytics in the e-commerce platform.

## Overview

The Order Service handles:
- Order creation with saga orchestration
- Order status management and updates
- Order cancellation (buyers and sellers)
- Purchase analytics for buyers
- Sales analytics for sellers
- Reorder functionality for delivered orders
- Order status history tracking
- Seller order management

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.4.0
- **Language**: Java 21
- **Database**: MongoDB
- **Message Broker**: Apache Kafka
- **Service Discovery**: Netflix Eureka
- **HTTP Client**: OpenFeign
- **Security**: JWT-based authentication

### Key Components

#### Controllers
- `OrdersController` - REST API endpoints for order management

#### Services
- `OrderService` - Core business logic for order operations
- `OrderSagaOrchestrator` - Saga pattern for distributed transactions
- `JwtService` - JWT token validation and extraction
- `JwtAuthenticationFilter` - Authentication filter

#### Models
- `Order` - Order entity with items and metadata
- `OrderItem` - Individual order line items
- `User` - User reference
- `Product` - Product reference
- `StatusHistory` - Order status change history

#### DTOs
- `OrderDTO` - Create order request
- `OrderStatusResponseDTO` - Status update response
- `OrderStatusUpdateDTO` - Status update request
- `PurchaseSummaryDTO` - Purchase analytics summary
- `SellerAnalyticsSummaryDTO` - Sales analytics summary
- `ReorderResponseDTO` - Reorder items response

#### Clients
- `ProductClient` - Feign client for Product Service communication

## Kafka Topics

The Order Service publishes and consumes the following Kafka topics:

### Published Events
- `order.created` - New order created event
- `order.product.event` - Product-related order events
- `order.inventory.release` - Inventory release event on cancellation

### Consumed Events
- `products.reservation.success` - Product reservation successful
- `products.reservation.failed` - Product reservation failed

## Order Status Workflow

### Order Statuses
- **PENDING**: Order created, awaiting seller processing
- **PROCESSING**: Order being prepared by seller
- **SHIPPED**: Order dispatched to buyer
- **DELIVERED**: Order successfully delivered
- **CANCELLED**: Order cancelled by buyer or seller
- **FAILED**: Order processing failed

### Status Transitions
- PENDING → PROCESSING (seller)
- PENDING → CANCELLED (buyer or seller)
- PROCESSING → SHIPPED (seller)
- PROCESSING → CANCELLED (buyer or seller)
- SHIPPED → DELIVERED (seller or system)
- Any status → FAILED (system error)

### Saga Flow
1. Buyer creates order
2. Service publishes `order.created` event
3. Product Service listens and reserves inventory
4. Product Service publishes success/failure event
5. Order Service handles response:
   - Success: Confirm order
   - Failure: Cancel order with reason
6. Status updates trigger `order.product.event` events

## API Endpoints

### Order Management

#### Get User Orders
```http
GET /orders?page=0&size=10&sortBy=createdAt&sortDir=desc&status=DELIVERED&startDate=2026-01-01&endDate=2026-01-31
Authorization: Bearer <token>
```

Query Parameters:
- `page`: Page number (default: 0)
- `size`: Page size, max 100 (default: 10)
- `sortBy`: Sort field - createdAt, totalPrice, status (default: createdAt)
- `sortDir`: Sort direction - asc, desc (default: desc)
- `status`: Filter by order status (optional)
- `startDate`: ISO date format YYYY-MM-DD (optional)
- `endDate`: ISO date format YYYY-MM-DD (optional)

#### Create Order
```http
POST /orders
Content-Type: application/json
Authorization: Bearer <token>

{
  "email": "buyer@example.com",
  "internationalPhone": "+1234567890",
  "fullName": "John Doe",
  "address": "123 Main St",
  "city": "New York",
  "postalCode": "10001",
  "orderItems": [
    {
      "productId": "prod123",
      "quantity": 2
    }
  ]
}
```

#### Get Order Details
```http
GET /orders/{orderId}
Authorization: Bearer <token>
```

#### Update Order Status (Sellers Only)
```http
PATCH /orders/{orderId}/status
Content-Type: application/json
Authorization: Bearer <token>

{
  "status": "SHIPPED"
}
```

#### Cancel Order
```http
PATCH /orders/{orderId}/cancel
Content-Type: application/json
Authorization: Bearer <token>

{
  "reason": "No longer needed"
}
```

### Reorder Functionality

#### Get Items for Reorder
```http
GET /orders/{orderId}/reorder
Authorization: Bearer <token>
```

Returns:
```json
{
  "availableItems": [...],
  "unavailableItems": [...],
  "warnings": [
    "Only 3 of 5 'Product Name' available (limited stock)",
    "Price changed for 'Product Name': 9.99 → 12.99"
  ],
  "originalOrderId": "order-123",
  "fetchedAt": "2026-01-31T10:30:00.123456"
}
```

### Seller Features

#### Get Seller Orders
```http
GET /orders/seller?page=0&size=10&sortBy=createdAt&sortDir=desc
Authorization: Bearer <token>
```

Returns orders containing seller's products with same query parameters as user orders.

#### Purchase Analytics
```http
GET /orders/analytics/purchase-summary?status=DELIVERED&startDate=2026-01-01&endDate=2026-01-31
Authorization: Bearer <token>
```

Returns:
```json
{
  "totalSpent": 1500.50,
  "orderCount": 25,
  "productCount": 67,
  "dateRange": {
    "start": "2026-01-01",
    "end": "2026-01-31"
  },
  "mostPurchasedProducts": [...],
  "topSpendingProducts": [...]
}
```

#### Seller Analytics
```http
GET /orders/analytics/seller-summary?status=DELIVERED&startDate=2026-01-01&endDate=2026-01-31
Authorization: Bearer <token>
```

Returns:
```json
{
  "totalRevenue": 5000.00,
  "totalOrders": 50,
  "totalUnitsSold": 200,
  "productCount": 15,
  "dateRange": {
    "start": "2026-01-01",
    "end": "2026-01-31"
  },
  "bestSellingProducts": [...],
  "topRevenueProducts": [...]
}
```

## Database Schema

### Order Collection
```javascript
{
  "_id": ObjectId,
  "buyerId": String,
  "email": String,
  "internationalPhone": String,
  "fullName": String,
  "address": String,
  "city": String,
  "postalCode": String,
  "orderItems": [
    {
      "productId": String,
      "productName": String,
      "quantity": Number,
      "price": Number
    }
  ],
  "totalPrice": Number,
  "status": String (enum),
  "createdAt": ISODate,
  "updatedAt": ISODate,
  "statusHistory": [
    {
      "status": String,
      "changedAt": ISODate
    }
  ]
}
```

## Integration Points

### Product Service
- **Feign Client**: `ProductClient`
- **Purpose**: Fetch product details for order validation
- **Endpoints Used**:
  - GET `/products/{id}` - Get product by ID
  - GET `/products/ids` - Get multiple products by IDs

### User Service
- **Purpose**: User authentication and role verification
- **Integration**: JWT token validation and user ID/role extraction

### Media Service
- **Purpose**: Product image handling for reorder functionality
- **Integration**: Fetch product images from media storage

### MongoDB
- **Database**: `esouq`
- **Collection**: `orders`
- **Indexes**: buyerId, status, createdAt

### Kafka
- **Bootstrap Servers**: localhost:9092 (or configured via environment)
- **Consumer Group**: `orderservice-group`
- **Auto Offset Reset**: earliest

## Security

### Authentication
- JWT-based authentication required for all endpoints
- Token validation via `JwtAuthenticationFilter`
- User ID and role extracted from JWT claims

### Authorization
- **Buyers**: Can view/cancel their own orders, view purchase analytics
- **Sellers**: Can view orders containing their products, update order status, view seller analytics
- **Admins**: Full access to all endpoints

### Role Checks
- CLIENT/BUYER: Order creation, cancellation, purchase analytics
- SELLER: Status updates, seller orders, seller analytics
- ADMIN: All operations

## Local Development

### Prerequisites
- Java 21+
- Maven 3.8+
- MongoDB running on localhost:27017
- Kafka running on localhost:9092
- Eureka Server running on localhost:8761

### Configuration Files
- `application.properties` - Default configuration
- `application-docker.properties` - Docker deployment configuration

### Running Locally
```bash
# From backend directory
cd order-service
./mvnw spring-boot:run
```

### Environment Variables
- `EUREKA_URI`: Eureka server URL (default: http://localhost:8761/eureka)
- `spring.data.mongodb.*`: MongoDB connection settings
- `spring.kafka.*`: Kafka configuration

## Testing

### Run Tests
```bash
./mvnw test
```

### Test Coverage
- Unit tests for service layer
- Integration tests for controllers
- Saga orchestration tests
- Kafka event handling tests

### JaCoCo Code Coverage
```bash
./mvnw test jacoco:report
```

Report generated at: `target/site/jacoco/index.html`

## Error Handling

### Custom Exceptions
- `OrderCannotBeCancelledException` - Invalid order state for cancellation
- `UnauthorizedOrderAccessException` - User not authorized for operation
- `ResponseStatusException` - HTTP error responses

### HTTP Status Codes
- **200 OK**: Successful operation
- **400 Bad Request**: Invalid request parameters
- **401 Unauthorized**: Authentication/authorization failure
- **404 Not Found**: Order not found
- **500 Internal Server Error**: Server error

## Monitoring & Logging

### Logging Levels
- Controller: INFO
- Service: DEBUG (in dev)
- Feign Client: DEBUG

### Important Logs
- Order creation requests
- Status update requests
- Saga orchestration steps
- Kafka event publishing/consuming
- Error scenarios

## Troubleshooting

### Common Issues

**Order not registering with Eureka**
- Check Eureka server is running
- Verify Eureka URI configuration
- Check network connectivity

**Kafka connection failures**
- Verify Kafka is running on configured port
- Check Kafka topic configuration
- Review consumer group settings

**Database connection errors**
- Ensure MongoDB is running
- Verify MongoDB credentials
- Check network connectivity to MongoDB

**Saga failures**
- Review Product Service logs
- Check Kafka event flow
- Verify inventory reservation logic

## References

- [Frontend Integration Guide](../../../frontend/REORDER_API_SPEC.md)
- [Product Service Documentation](../product-service/README.md)
- [Main Project README](../../../README.md)
- [Code Review Guidelines](../../../CODE_REVIEW_GUIDELINES.md)
