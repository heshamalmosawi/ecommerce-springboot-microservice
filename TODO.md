# TODO - Nexus Learning Project

## Project Information

**Project Name**: ecommerce-microservices (buy-02)
**Location**: /home/hesham/ecommerce-microservices
**Services**: 6 microservices (eureka-service-discovery, apigateway, user-service, product-service, media-service, order-service)
**Java Version**: 21 (meets Java 11+ requirement)
**Build Tool**: Maven with Maven Wrapper (mvnw)
**CI/CD**: Jenkins (Jenkinsfile exists)

---

## Required Tasks

### Phase 1: Environment Setup
- [X] Install Java 11 or higher on your system
- [X] Install Maven compatible with Java 11
- [X] Verify Java installation works (`java -version`)
- [X] Verify Maven installation works (`mvn -version`)

### Phase 2: Nexus Installation
- [X] Download the latest Nexus Repository Manager release
- [X] Create a dedicated "nexus" user account on the system
- [X] Install Nexus Repository Manager under the nexus user (not root)
- [X] Start the Nexus service
- [X] Access the Nexus web interface
- [X] Change the default admin password

### Phase 3: Nexus Post-Installation Setup
- [X] Verify Nexus service is running correctly
- [X] Explore Nexus web interface
- [X] Understand repository types (hosted, proxy, group)

### Phase 4: Application Setup ✅
- [X] Verify ecommerce-microservices project is buy-02 project
- [X] Verify project is configured as multi-module Maven project
- [X] Verify all 6 services have pom.xml files
- [X] Verify all services have Dockerfiles
- [X] Build project successfully (`cd backend && mvn clean package`)
- [X] Verify project runs successfully

### Phase 5: Maven-Nexus Configuration
- [X] Create a Maven group repository in Nexus for resolving dependencies
- [X] Configure Maven authentication for Nexus in `~/.m2/settings.xml`
- [X] Update parent POM (`backend/pom.xml`) with Nexus repositories
- [X] Update all module POMs to inherit Nexus configuration:
  - [X] eureka-service-discovery
  - [X] apigateway
  - [X] user-service
  - [X] product-service
  - [X] media-service
  - [X] order-service
- [X] Test Maven connection to Nexus repositories for all services

### Phase 6: Artifact Publishing
- [X] Create a hosted Maven repository in Nexus for storing artifacts
- [X] Build all microservice artifacts (`cd backend && mvn clean package`)
- [X] Deploy all 6 service artifacts to Nexus (`mvn deploy`)
- [X] Verify all artifacts appear in Nexus web interface:
  - [X] productservice
  - [X] userservice
  - [X] mediaservice
  - [X] orderservice
  - [X] eureka-service-discovery
  - [X] apigateway

### Phase 7: Dependency Management
- [X] Create a proxy Maven repository in Nexus for external dependencies
- [X] Configure all services to use Nexus as proxy for external dependencies
- [X] Remove direct access to Maven Central from project
- [X] Build project to trigger dependency resolution through Nexus
- [X] Verify external dependencies are cached in Nexus

### Phase 8: Versioning
- [X] Update application version in parent POM (backend/pom.xml)
- [X] Update individual module versions if needed
- [X] Deploy all new versions to Nexus
- [X] Retrieve and verify both versions from Nexus
- [X] Demonstrate version management in Nexus interface for all services

### Phase 9: Docker Integration
- [ ] Create a hosted Docker registry repository in Nexus
- [ ] Build Docker images for all 6 services
- [ ] Configure Docker to use Nexus Docker registry
- [ ] Tag all Docker images for Nexus registry
- [ ] Push all 6 Docker images to Nexus
- [ ] Verify all Docker images appear in Nexus:
  - [ ] productservice
  - [ ] userservice
  - [ ] mediaservice
  - [ ] orderservice
  - [ ] eureka-server
  - [ ] api-gateway

### Phase 10: CI/CD Pipeline
- [ ] Review existing Jenkinsfile configuration
- [ ] Configure Jenkins with Nexus admin credentials
- [ ] Add Nexus artifact deployment stage to Jenkins pipeline
- [ ] Add Docker image publishing to Jenkins pipeline
- [ ] Test complete CI pipeline with Nexus publishing
- [ ] Verify all artifacts and Docker images are published automatically

### Phase 11: Documentation
- [ ] Create project overview documentation
- [ ] Document Nexus installation and setup steps
- [ ] Document Maven configuration for Nexus integration (parent POM + module POMs)
- [ ] Document artifact publishing process for all 6 services
- [ ] Document dependency management setup
- [ ] Document versioning process
- [ ] Document Docker integration steps for all services
- [ ] Document Jenkinsfile modifications
- [ ] Document CI/CD pipeline configuration
- [ ] Add screenshots to documentation
- [ ] Add usage examples to documentation

---

## Optional Tasks (Bonus)

### Security & Access Control
- [ ] Create additional user accounts in Nexus
- [ ] Set up roles with different permission levels
- [ ] Configure repository-level permissions
- [ ] Restrict access to specific artifacts
- [ ] Test authentication and authorization
- [ ] Document security configuration
