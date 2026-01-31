# eSouq E-commerce Microservices

A modern e-commerce platform built with Spring Boot microservices and Angular frontend.

## Architecture

- **Frontend**: Angular 20 (TypeScript, SCSS)
- **Backend**: Spring Boot microservices with Eureka service discovery
- **Database**: MongoDB
- **Messaging**: Apache Kafka
- **API Gateway**: Spring Cloud Gateway with SSL
- **Code Quality**: SonarQube for static analysis and security scanning
- **CI/CD**: Jenkins pipeline with automated testing and deployment

## Services

| Service | Description |
|---------|-------------|
| Frontend | Angular application |
| API Gateway | Secure gateway to all services |
| User Service | User management & authentication |
| Product Service | Product catalog management |
| Order Service | Order management with saga orchestration |
| Media Service | Media file handling |
| Eureka Server | Service discovery |
| MongoDB | Data persistence |
| Kafka | Event streaming |
| SonarQube | Code quality analysis and security scanning |
| Jenkins | CI/CD pipeline automation |

## Quick Start

### Prerequisites
- Java 21+
- Node.js 18+
- Maven 3.8+
- Docker & Docker Compose
- Git (for cloning)
- Google Chrome (for testing)
- Jenkins (for CI/CD pipeline)
- SonarQube Server (for code quality analysis)

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
- **SonarQube Dashboard**: http://localhost:9000
- **Backend Analysis**: http://localhost:9000/dashboard?id=esouq
- **Frontend Analysis**: http://localhost:9000/dashboard?id=ecommerce-frontend

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
   docker compose -f db/docker compose.yml up -d
   docker compose -f kafka/docker compose.yml up -d
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

### Core Services
- `https://localhost:8443/users/*` - User management
- `https://localhost:8443/products/*` - Product catalog  
- `https://localhost:8443/media/*` - Media handling

### Order Management
- `GET https://localhost:8443/orders` - Get user orders (with filtering: status, date range, pagination)
- `POST https://localhost:8443/orders` - Create new order
- `GET https://localhost:8443/orders/{orderId}` - Get order details
- `PATCH https://localhost:8443/orders/{orderId}/status` - Update order status (sellers)
- `PATCH https://localhost:8443/orders/{orderId}/cancel` - Cancel order (buyers & sellers)

### Reorder Functionality
- `GET https://localhost:8443/orders/{orderId}/reorder` - Get items available for reorder from delivered orders

### Seller Features
- `GET https://localhost:8443/orders/seller` - Get seller's orders (orders containing seller's products)
- `GET https://localhost:8443/orders/analytics/purchase-summary` - Purchase analytics for buyers
- `GET https://localhost:8443/orders/analytics/seller-summary` - Sales analytics for sellers

## 👥 Seller Features

### Seller Dashboard
Sellers can access specialized tools for managing their products and orders:

- **Product Management**: Add, edit, and manage product listings
- **Order Management**: View and update status of orders containing seller's products
- **Sales Analytics**: Track revenue, best-selling products, and order statistics
- **Inventory Control**: Monitor product availability and stock levels

### Seller Order Workflow
1. Seller views orders through `/seller/orders` endpoint
2. Seller can update order status: PENDING → PROCESSING → SHIPPED → DELIVERED
3. Seller can cancel orders in PENDING or PROCESSING status
4. Automatic inventory release when orders are cancelled
5. Real-time order status history tracking

### Order Status Flow
- **PENDING**: Order created, awaiting seller processing
- **PROCESSING**: Order being prepared by seller
- **SHIPPED**: Order dispatched to buyer
- **DELIVERED**: Order successfully delivered to buyer
- **CANCELLED**: Order cancelled by buyer or seller
- **FAILED**: Order processing failed

## 🔄 Reorder Functionality

Buyers can quickly repurchase items from their delivered orders:

### Features
- **One-Click Reorder**: Access all items from past orders
- **Availability Check**: Real-time stock verification
- **Price Comparison**: Shows price changes since original purchase
- **Stock Warnings**: Alerts for limited availability
- **Partial Reorder**: Add only available items if some are out of stock

### Reorder Process
1. Buyer navigates to order history
2. Clicks "Reorder" button on delivered orders
3. System checks current product availability and pricing
4. Shows available items with warnings for price changes/limited stock
5. Buyer confirms to add available items to cart
6. Proceed to checkout

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

## 🔍 Code Quality & Security

### SonarQube Integration
The project uses SonarQube for automated code quality analysis and security scanning:

#### Setup Requirements
```bash
# Start SonarQube server
docker run --name sonarqube -p 9000:9000 sonarqube:latest

# Access dashboard at: http://localhost:9000
# Default credentials: admin/admin
```

#### Analysis Process
- **Backend Analysis**: Maven-based SonarQube integration with Java code scanning
- **Frontend Analysis**: SonarScanner CLI for TypeScript/JavaScript code analysis
- **Security Scanning**: Automated vulnerability and bug detection
- **Quality Gates**: Automated quality thresholds with pipeline integration

#### Quality Metrics Tracked
- **Code Smells**: Maintainability issues and code quality problems
- **Bugs**: Potential runtime errors and logic issues
- **Vulnerabilities**: Security vulnerabilities and security hotspots
- **Coverage**: Test coverage analysis (where available)
- **Duplications**: Code redundancy detection

### Continuous Quality Monitoring
- **Automated Analysis**: Runs on every Jenkins build
- **Dashboard Access**: Real-time quality metrics at http://localhost:9000
- **Trend Tracking**: Historical quality data and improvements
- **Integration**: Seamlessly integrated with CI/CD pipeline

## 🔐 Code Review & Approval Process

The project implements a structured code review and approval process for maintaining code quality and security. 

**See [CODE_REVIEW_GUIDELINES.md](CODE_REVIEW_GUIDELINES.md) for detailed guidelines, pull request process, and approval requirements.**

### Key Features
- **Branch Protection**: Direct pushes to main are blocked
- **Pull Requirement**: All changes must go through PR process  
- **Quality Gates**: SonarQube analysis must pass before merge
- **Admin Restrictions**: Even repository admins must follow review process
- **Automated Quality**: Code quality analysis integrated with GitHub workflow

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

## 🔄 CI/CD Pipeline

### Jenkins Pipeline Overview
The `Jenkinsfile` implements a comprehensive CI/CD pipeline with:

- **Multi-stage Build**: Frontend and backend compilation and testing
- **Docker Integration**: Automated container builds and deployments
- **Rollback Capability**: Automatic rollback on deployment failures
- **Email Notifications**: Build status alerts with detailed reports
- **SonarQube Integration**: Automated code quality analysis and security scanning
- **Quality Gates**: Automated quality checks with fallback mechanisms

### Pipeline Stages
1. **Checkout & Setup**: Git operations and environment validation
2. **📋 Info**: Build information and tool version checks
3. **Backend build & test**: Spring Boot compilation and testing
4. **Frontend build & test**: Angular compilation and testing
5. **SonarQube Analysis**: Backend and frontend code quality analysis
6. **Quality Gate**: Automated quality check with fallback handling
7. **Docker Operations**: Build, deploy with rollback capability

## 📝 Development Notes

- **Service Registration**: Auto-register with Eureka on startup
- **Load Balancing**: Spring Cloud Gateway with service discovery
- **SSL Termination**: API Gateway handles HTTPS, frontend serves HTTPS
- **Event Streaming**: Kafka topics created via `kafka/kafka-topics.sh`
- **CORS**: Configured for HTTPS origins only
- **Certificate Trust**: Frontend trusts API Gateway certificate in container
- **SonarQube Token**: Authentication token configured in Jenkins pipeline
- **Quality Analysis**: Automated code quality checks on every build
- **Rollback Support**: Jenkins pipeline includes automatic rollback on deployment failures


