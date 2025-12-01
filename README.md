# eSouq E-commerce Microservices

A modern e-commerce platform built with Spring Boot microservices and Angular frontend.

## Architecture

- **Frontend**: Angular 20 (TypeScript, SCSS)
- **Backend**: Spring Boot microservices with Eureka service discovery
- **Database**: MongoDB
- **Messaging**: Apache Kafka
- **API Gateway**: Spring Cloud Gateway with SSL

## Services

| Service | Description |
|---------|-------------|
| Frontend | Angular application |
| API Gateway | Secure gateway to all services |
| User Service | User management & authentication |
| Product Service | Product catalog management |
| Media Service | Media file handling |
| Eureka Server | Service discovery |
| MongoDB | Data persistence |
| Kafka | Event streaming |

## Quick Start

### Prerequisites
- Java 17+
- Node.js 18+
- Maven 3.8+
- Docker & Docker Compose
- Git (for cloning)

## 🚀 Production Deployment (Recommended)

**Complete Docker Deployment - All Services Containerized**

```bash
# Clone and start entire application
git clone <repository-url>
cd ecommerce-microservices
docker compose up -d
```

This starts:
- All infrastructure services (MongoDB, Kafka, Zookeeper)
- All backend microservices with SSL
- Frontend with HTTPS serving
- Proper networking and service discovery

### Access Points

- **Frontend**: https://localhost:2122 *(HTTPS with self-signed cert)*
- **API Gateway**: https://localhost:8443
- **Eureka Dashboard**: http://localhost:8761
- **Mongo Express**: http://localhost:8081

## 🔧 Local Development

### Option 1: Mixed Development (Docker + Local)

1. **Start Infrastructure Only**
   ```bash
   docker compose up -d mongodb kafka zookeeper eureka-server
   ```

2. **Start Backend Locally**
   ```bash
   # Start microservices in separate terminals
   cd backend/user-service && mvn spring-boot:run
   cd backend/product-service && mvn spring-boot:run
   cd backend/media-service && mvn spring-boot:run
   cd backend/apigateway && mvn spring-boot:run
   ```

3. **Start Frontend Locally**
   ```bash
   cd frontend && ng serve
   ```

### Option 2: Full Local Development

1. **Start Infrastructure Services**
   ```bash
   docker-compose -f db/docker-compose.yml up -d
   docker-compose -f kafka/docker-compose.yml up -d
   ```

2. **Start Backend Services**
   ```bash
   # Start Eureka first (required)
   cd backend/eureka-service-discovery && mvn spring-boot:run
   
   # Start microservices in separate terminals
   cd ../user-service && mvn spring-boot:run
   cd ../product-service && mvn spring-boot:run
   cd ../media-service && mvn spring-boot:run
   cd ../apigateway && mvn spring-boot:run
   ```

3. **Start Frontend**
   ```bash
   cd frontend && ng serve
   ```

## 🌐 API Endpoints

All APIs are accessible through the API Gateway:

- `https://localhost:8443/users/*` - User management
- `https://localhost:8443/products/*` - Product catalog  
- `https://localhost:8443/media/*` - Media handling

## 🔒 SSL Configuration

### Production Docker Deployment
**Frontend SSL**: Containerized HTTPS serving
- Certificate: `frontend/certs/angular-dev.crt`
- Key: `frontend/certs/angular-dev.key`
- Port: 2122
- Certificate Trust: Configured via `NODE_EXTRA_CA_CERTS`

**Backend SSL**: API Gateway with Java keystore
- Keystore: `backend/apigateway/src/main/resources/spring-dev.p12`
- Password: `esouq123`
- Alias: `esouq`
- Port: 8443

### Local Development
**Frontend SSL**: Angular dev server configured in `frontend/angular.json`
- Certificate: `frontend/certs/angular-dev.crt`
- Key: `frontend/certs/angular-dev.key`
- Port: 4200

## 🏗️ Container Architecture

### Docker Network
- **Network**: `backend-network` (172.21.0.0/16)
- **Service Discovery**: Container name resolution
- **SSL Communication**: Frontend → API Gateway (HTTPS)

### Build Process
- **Frontend**: Multi-stage Node.js build with Alpine Linux
- **Backend**: Eclipse Temurin JDK 24 with Maven builds
- **Optimization**: .dockerignore for efficient builds

### Volumes
- **MongoDB Data**: `mongo_data` (persistent)
- **Media Storage**: `media_storage` (persistent)

## 📝 Development Notes

- **Service Registration**: Auto-register with Eureka on startup
- **Load Balancing**: Spring Cloud Gateway with service discovery
- **SSL Termination**: API Gateway handles HTTPS, frontend serves HTTPS
- **Event Streaming**: Kafka topics created via `kafka/kafka-topics.sh`
- **CORS**: Configured for HTTPS origins only
- **Certificate Trust**: Frontend trusts API Gateway certificate in container

