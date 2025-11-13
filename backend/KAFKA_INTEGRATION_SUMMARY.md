# Kafka Integration for Avatar/Product Image Processing

## Overview
This implementation introduces event-driven architecture using Apache Kafka to handle avatar and product image processing asynchronously. The system decouples media processing from user and product services, improving scalability and maintainability.

## Architecture Flow

### Avatar Processing Flow
```
Frontend → User Service → Kafka Topic → Media Service → MongoDB → User Service
```

1. **User Registration/Update**: Frontend sends base64 avatar data to User Service
2. **Event Publishing**: User Service publishes `user.avatar.upload` event to Kafka
3. **Media Processing**: Media Service consumes event, validates and stores base64 data
4. **Database Update**: Media Service saves media record to MongoDB with metadata
5. **Completion Event**: Media Service publishes `media.uploaded` event
6. **User Update**: User Service consumes completion event, updates user's avatarMediaId

### Product Image Processing Flow
```
Frontend → Product Service → Kafka Topic → Media Service → MongoDB → Product Service
```

Similar flow for product images using `product.image.upload` topic.

## Services Changes

### User Service
- **New Dependencies**: Spring Kafka, Jackson Databind
- **New Services**:
  - `AvatarEventService`: Publishes avatar-related events
  - `MediaEventConsumerService`: Consumes media processed events
- **Model Changes**: Added `avatarMediaId` field to User model
- **Configuration**: Kafka producer and consumer properties

### Media Service
- **New Dependencies**: Spring Kafka, Jackson Databind, Spring Validation
- **New Services**:
  - `AvatarProcessingService`: Processes avatar upload/update/delete events
  - `ProductImageProcessingService`: Processes product image events
  - `MediaService`: Business logic for media operations
- **New Repository**: `MediaRepository` for database operations
- **Configuration**: Enhanced Kafka consumer configuration

### Product Service
- **New Dependencies**: Spring Kafka, Jackson Databind
- **New Services**:
  - `ProductImageEventService`: Publishes product image events
  - `ProductMediaEventConsumerService`: Consumes media processed events
  - `MediaServiceClient`: Communicates with media service via Eureka
- **Model Changes**: Added `imageMediaIds` list to Product model
- **Configuration**: Kafka producer and consumer properties

## Kafka Topics

### User Avatar Topics
- `user.avatar.upload`: New avatar uploads
- `user.avatar.update`: Avatar updates
- `user.avatar.delete`: Avatar deletions

### Product Image Topics
- `product.image.upload`: New product images
- `product.image.update`: Product image updates
- `product.image.delete`: Product image deletions

### Media Processing Topics
- `media.uploaded`: Media processing completed events

## Database Schema Changes

### Users Collection
```json
{
  "_id": "user_id",
  "name": "User Name",
  "email": "user@example.com",
  "password": "hashed_password",
  "role": "seller",
  "avatarMediaId": "media_reference_id"
}
```

### Products Collection
```json
{
  "_id": "product_id",
  "name": "Product Name",
  "description": "Description",
  "price": 99.99,
  "quantity": 10,
  "userId": "seller_id",
  "imageMediaIds": ["media_id_1", "media_id_2"]
}
```

### Media Collection (Enhanced)
```json
{
  "_id": "media_id",
  "base64_data": "iVBORw0KGgoAAAANSUhEUgAA...",
  "content_type": "image/jpeg",
  "file_size_bytes": 2048576,
  "file_name": "image_17031234567890.jpg",
  "upload_timestamp": 17031234567890,
  "media_type": "avatar",
  "owner_id": "user_123"
}
```

## REST API Endpoints

### Media Service
- `POST /api/media/upload` - Upload media with validation
- `GET /api/media/{id}` - Retrieve media by ID
- `GET /api/media/owner/{ownerId}` - Get all media by owner
- `DELETE /api/media/{id}` - Delete media

## Validation & Security

### File Validation
- **Size Limit**: 2MB maximum (configurable via `media.max-file-size-mb`)
- **Content Types**: JPEG, JPG, PNG, GIF, WebP only
- **Base64 Validation**: Proper format checking
- **Media Types**: "avatar", "product_image" only

### Error Handling
- **Custom Exceptions**: `MediaValidationException` for bad requests
- **HTTP Status Codes**: 400, 413, 415, 404, 500
- **Graceful Degradation**: Returns null for failed media fetches

## Service Discovery

### Eureka Integration
- **Product Service**: Uses `DiscoveryClient` to find media service instances
- **Dynamic URLs**: No hardcoded IPs, uses service discovery
- **Load Balancing**: Ready for multiple media service instances
- **Failover Support**: Automatic fallback to available instances

## Code Quality Improvements

### Lombok Integration
- **Event Classes**: All use `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- **Boilerplate Reduction**: 200+ lines of manual getters/setters eliminated
- **Type Safety**: Compile-time checking for all fields
- **Consistency**: Uniform approach across all services

### Architecture Fixes
- **Data Ownership**: Fixed unused parameters in `processAvatarData` and `processProductImageData`
- **Metadata Storage**: Complete media information (size, timestamps, ownership)
- **Clean Configuration**: Removed unused `storageRoot` property
- **Service Communication**: Proper Eureka-based service discovery

## Benefits

1. **Asynchronous Processing**: Non-blocking avatar/image uploads
2. **Scalability**: Media processing can scale independently
3. **Decoupling**: Services don't directly depend on each other
4. **Resilience**: Failed uploads can be retried via Kafka
5. **Audit Trail**: All media changes logged as events
6. **Separation of Concerns**: Clear boundaries between services
7. **Data Integrity**: Complete metadata tracking with ownership
8. **Service Discovery**: Dynamic service location via Eureka

## Future Enhancements

1. **Retry Logic**: Implement exponential backoff for failed events
2. **Dead Letter Queues**: Handle problematic events
3. **Image Optimization**: Add image resizing/compression
4. **CDN Integration**: Store files in cloud storage
5. **Event Sourcing**: Full audit trail of all changes
6. **Monitoring**: Add metrics and health checks
7. **Caching**: Redis cache for frequently accessed media
8. **Security**: JWT-based authentication for media endpoints

## Testing

To test the implementation:

1. Start all services (Eureka, API Gateway, User, Product, Media)
2. Ensure Kafka is running on localhost:9092
3. Register a seller with avatar data
4. Verify avatar is processed and stored with metadata
5. Create product with images
6. Verify images are processed and linked to products
7. Test service discovery by stopping/starting media service
8. Validate file size limits and content type restrictions

All services compile successfully and are ready for deployment with proper base64 storage, validation, and service discovery.