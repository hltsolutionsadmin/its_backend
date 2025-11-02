#!/bin/bash

# Stop Services Script
# This script stops all running microservices

echo "🛑 Stopping Jira Clone Microservices..."
echo "========================================"

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Function to stop a service
stop_service() {
    SERVICE_NAME=$1
    PID_FILE="pids/$SERVICE_NAME.pid"
    
    if [ -f "$PID_FILE" ]; then
        PID=$(cat $PID_FILE)
        if ps -p $PID > /dev/null 2>&1; then
            echo -e "Stopping $SERVICE_NAME (PID: $PID)..."
            kill $PID
            sleep 2
            
            # Force kill if still running
            if ps -p $PID > /dev/null 2>&1; then
                echo -e "${RED}Force stopping $SERVICE_NAME${NC}"
                kill -9 $PID
            fi
            
            echo -e "${GREEN}✓ $SERVICE_NAME stopped${NC}"
        else
            echo -e "${RED}✗ $SERVICE_NAME is not running${NC}"
        fi
        rm -f $PID_FILE
    else
        echo -e "${RED}✗ No PID file found for $SERVICE_NAME${NC}"
    fi
}

# Stop services in reverse order
echo "Stopping services..."
echo ""

if [ -d "api-gateway" ]; then
    stop_service "api-gateway"
fi

stop_service "issue-service"
stop_service "user-service"
stop_service "discovery-service"

echo ""
echo "========================================"
echo -e "${GREEN}✓ All services stopped${NC}"

# Clean up PID directory if empty
if [ -d "pids" ]; then
    rmdir pids 2>/dev/null
fi

echo ""
echo "To start services again, run: ./START_SERVICES.sh"
