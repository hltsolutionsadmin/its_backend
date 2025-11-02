#!/bin/bash

# Start Services Script
# This script starts all microservices in the correct order

echo "🚀 Starting Jira Clone Microservices..."
echo "========================================"

# Color codes
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Ensure Java 17 is available and used (macOS)
if command -v /usr/libexec/java_home &> /dev/null; then
    JAVA_17_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null)
    if [ -z "$JAVA_17_HOME" ]; then
        echo "❌ JDK 17 not found. Please install Temurin/OpenJDK 17."
        echo "   macOS (Homebrew): brew install --cask temurin17"
        exit 1
    fi
    export JAVA_HOME="$JAVA_17_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"
else
    echo "⚠️  Could not detect /usr/libexec/java_home. Ensure JAVA_HOME points to JDK 17."
    if ! java -version 2>&1 | grep -q '17\.'; then
        echo "❌ Active Java is not 17. Please set JAVA_HOME to JDK 17 and retry."
        exit 1
    fi
fi

# Function to start a service
start_service() {
    SERVICE_NAME=$1
    PORT=$2
    
    echo -e "${YELLOW}Starting $SERVICE_NAME on port $PORT...${NC}"
    cd $SERVICE_NAME
    mvn spring-boot:run > ../logs/$SERVICE_NAME.log 2>&1 &
    SERVICE_PID=$!
    echo $SERVICE_PID > ../pids/$SERVICE_NAME.pid
    cd ..
    echo -e "${GREEN}✓ $SERVICE_NAME started (PID: $SERVICE_PID)${NC}"
}

# Create directories for logs and PIDs
mkdir -p logs
mkdir -p pids

# Build all services first
echo -e "${YELLOW}Building all services with JDK at $JAVA_HOME (skipping tests and test compilation)...${NC}"
JAVA_HOME="$JAVA_HOME" mvn clean package -DskipTests -Dmaven.test.skip=true
if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please fix errors and try again."
    exit 1
fi
echo -e "${GREEN}✓ Build successful${NC}"
echo ""

# Start services in order
echo "Starting services..."
echo ""

# 1. Discovery Service (must start first)
start_service "discovery-service" 8761
sleep 15  # Wait for discovery service to be ready

# 2. User Service
start_service "user-service" 8081
sleep 10

# 3. Issue Service
start_service "issue-service" 8082
sleep 10

# 4. API Gateway (optional)
if [ -d "api-gateway" ]; then
    start_service "api-gateway" 8080
fi

echo ""
echo "========================================"
echo -e "${GREEN}✓ All services started successfully!${NC}"
echo ""
echo "Service URLs:"
echo "  • Eureka Dashboard: http://localhost:8761"
echo "  • User Service:     http://localhost:8081"
echo "  • Issue Service:    http://localhost:8082"
if [ -d "api-gateway" ]; then
    echo "  • API Gateway:      http://localhost:8080"
fi
echo ""
echo "Logs are available in the ./logs directory"
echo "To stop services, run: ./STOP_SERVICES.sh"
echo ""
echo "To view logs in real-time:"
echo "  tail -f logs/user-service.log"
echo "  tail -f logs/issue-service.log"
