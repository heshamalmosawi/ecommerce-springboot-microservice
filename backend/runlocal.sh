#!/bin/bash

# Script to run all microservices in the correct order
# Order: Eureka -> Core Services -> API Gateway

set -e  # Exit on error

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Base directory
BASE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo -e "${BLUE}==================================================${NC}"
echo -e "${BLUE}Starting E-commerce Microservices${NC}"
echo -e "${BLUE}==================================================${NC}"

# Function to start a service
start_service() {
    local service_name=$1
    local service_dir=$2
    
    echo -e "\n${GREEN}Starting ${service_name}...${NC}"
    cd "${BASE_DIR}/${service_dir}"
    
    # Start the service in background
    ./mvnw spring-boot:run > "/tmp/${service_name}.log" 2>&1 &
    
    echo -e "${GREEN}${service_name} started (PID: $!)${NC}"
    echo -e "${YELLOW}Logs: /tmp/${service_name}.log${NC}"
}

# 1. Start Eureka Service Discovery
echo -e "\n${BLUE}Step 1: Starting Eureka Service Discovery${NC}"
# start_service "eureka-service-discovery" "eureka-service-discovery"

# Wait 2 seconds for Eureka to initialize
echo -e "${YELLOW}Waiting 2 seconds for Eureka to start...${NC}"
sleep 2

# 2. Start Core Services (User, Product, Media, Order)
echo -e "\n${BLUE}Step 2: Starting Core Services${NC}"
start_service "user-service" "user-service"
start_service "product-service" "product-service"
start_service "media-service" "media-service"
# start_service "order-service" "order-service"

# Wait 8 seconds for core services to register with Eureka
echo -e "${YELLOW}Waiting 8 seconds for core services to start and register...${NC}"
sleep 8

# 3. Start API Gateway
echo -e "\n${BLUE}Step 3: Starting API Gateway${NC}"
echo -e "${GREEN}Starting apigateway...${NC}"
cd "${BASE_DIR}/apigateway"

echo -e "\n${GREEN}==================================================${NC}"
echo -e "${GREEN}All background services started successfully!${NC}"
echo -e "${GREEN}==================================================${NC}"
echo -e "\n${GREEN}Now starting API Gateway in foreground...${NC}"
./mvnw spring-boot:run
echo -e "\n${YELLOW}Service URLs:${NC}"
echo -e "  - Eureka Dashboard: http://localhost:8761"
echo -e "  - API Gateway:      http://localhost:8080"
echo -e "\n${YELLOW}To stop all services, run:${NC}"
echo -e "  pkill -f 'spring-boot:run'"
echo -e "\n${YELLOW}To view logs:${NC}"
# echo -e "  tail -f /tmp/eureka-service-discovery.log"
echo -e "  tail -f /tmp/user-service.log"
echo -e "  tail -f /tmp/product-service.log"
echo -e "  tail -f /tmp/media-service.log"
# echo -e "  tail -f /tmp/order-service.log"
echo -e "  tail -f /tmp/apigateway.log"
