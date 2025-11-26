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

## Option 1: Local Development

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

## Option 2: Docker Deployment

1. **Start Backend Services**
   ```bash
   # Start all services (infrastructure + backend)
   docker-compose -f docker-compose.backend.yml up -d
   ```
2. **Start Frontend**
   ```bash
   cd frontend && ng serve
   ```


### Access Points

- **Frontend**: https://localhost:4200 (local dev only)
- **API Gateway**: https://localhost:8443
- **Eureka Dashboard**: http://localhost:8761
- **Mongo Express**: http://localhost:8081 (Docker only)

## API Endpoints

All APIs are accessible through the API Gateway:

- `https://localhost:8443/users/*` - User management
- `https://localhost:8443/products/*` - Product catalog  
- `https://localhost:8443/media/*` - Media handling

## SSL Configuration

The application uses HTTPS by default:

**Frontend SSL**: Angular dev server configured in `frontend/angular.json`
- Certificate: `frontend/certs/angular-dev.crt`
- Key: `frontend/certs/angular-dev.key`

**Backend SSL**: API Gateway configured in `backend/apigateway/src/main/resources/application.properties`
- Keystore: `spring-dev.p12`
- Password: `esouq123`
- Alias: `esouq`

## Development Notes

- Services auto-register with Eureka on startup
- API Gateway routes requests to appropriate microservices
- CORS is configured for HTTPS origins only
- Kafka topics are created via `kafka/kafka-topics.sh`
- All services use HTTPS in development mode