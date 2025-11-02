#!/bin/bash

# Complete Functionality Verification Script
# This script tests all major features of the platform

set -e  # Exit on error

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Base URLs
USER_SERVICE="http://localhost:8081"
ISSUE_SERVICE="http://localhost:8082"

# Test results
PASSED=0
FAILED=0

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}🔍 Starting Complete Functionality Verification${NC}"
echo -e "${BLUE}========================================${NC}\n"

# Helper function to test endpoint
test_endpoint() {
    local name="$1"
    local url="$2"
    local method="$3"
    local data="$4"
    local headers="$5"
    local expected_status="${6:-200}"
    
    echo -n "Testing: $name... "
    
    if [ "$method" = "POST" ] || [ "$method" = "PUT" ]; then
        response=$(curl -s -w "\n%{http_code}" -X $method "$url" \
            -H "Content-Type: application/json" \
            $headers \
            -d "$data" 2>/dev/null || echo "000")
    else
        response=$(curl -s -w "\n%{http_code}" -X $method "$url" $headers 2>/dev/null || echo "000")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASSED${NC} (HTTP $http_code)"
        ((PASSED++))
        return 0
    else
        echo -e "${RED}✗ FAILED${NC} (Expected $expected_status, got $http_code)"
        ((FAILED++))
        return 1
    fi
}

# Check if services are running
echo -e "${YELLOW}Checking if services are running...${NC}"
if ! curl -s "$USER_SERVICE/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}✗ User Service is not running on port 8081${NC}"
    echo -e "${YELLOW}Please start services first: ./START_SERVICES.sh${NC}"
    exit 1
fi

if ! curl -s "$ISSUE_SERVICE/actuator/health" > /dev/null 2>&1; then
    echo -e "${RED}✗ Issue Service is not running on port 8082${NC}"
    echo -e "${YELLOW}Please start services first: ./START_SERVICES.sh${NC}"
    exit 1
fi

echo -e "${GREEN}✓ All services are running${NC}\n"

# ==========================================
# 1. AUTHENTICATION TESTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}1️⃣  Testing Authentication Flow${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Register a test user
TIMESTAMP=$(date +%s)
TEST_EMAIL="test${TIMESTAMP}@example.com"
TEST_USERNAME="testuser${TIMESTAMP}"
TEST_PASSWORD="test123456"

echo "Creating test user: $TEST_USERNAME"
register_response=$(curl -s -X POST "$USER_SERVICE/api/auth/register" \
    -H "Content-Type: application/json" \
    -d "{
        \"email\": \"$TEST_EMAIL\",
        \"username\": \"$TEST_USERNAME\",
        \"password\": \"$TEST_PASSWORD\",
        \"firstName\": \"Test\",
        \"lastName\": \"User\"
    }")

if echo "$register_response" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ User Registration${NC}"
    ((PASSED++))
    USER_ID=$(echo "$register_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
else
    echo -e "${RED}✗ User Registration FAILED${NC}"
    echo "$register_response"
    ((FAILED++))
    exit 1
fi

# Login
login_response=$(curl -s -X POST "$USER_SERVICE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{
        \"usernameOrEmail\": \"$TEST_USERNAME\",
        \"password\": \"$TEST_PASSWORD\"
    }")

if echo "$login_response" | grep -q '"accessToken"'; then
    echo -e "${GREEN}✓ User Login${NC}"
    ((PASSED++))
    TOKEN=$(echo "$login_response" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
    REFRESH_TOKEN=$(echo "$login_response" | grep -o '"refreshToken":"[^"]*' | cut -d'"' -f4)
else
    echo -e "${RED}✗ User Login FAILED${NC}"
    ((FAILED++))
    exit 1
fi

# Get current user
test_endpoint "Get Current User" \
    "$USER_SERVICE/api/users/me" \
    "GET" \
    "" \
    "-H 'Authorization: Bearer $TOKEN'"

echo ""

# ==========================================
# 2. ORGANIZATION TESTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}2️⃣  Testing Organization Management${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Create organization
org_response=$(curl -s -X POST "$USER_SERVICE/api/orgs" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"Test Organization ${TIMESTAMP}\",
        \"orgCode\": \"ORG${TIMESTAMP}\",
        \"description\": \"Test organization for verification\"
    }")

if echo "$org_response" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ Create Organization${NC}"
    ((PASSED++))
    ORG_ID=$(echo "$org_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
else
    echo -e "${RED}✗ Create Organization FAILED${NC}"
    ((FAILED++))
    exit 1
fi

# Get organization details
test_endpoint "Get Organization" \
    "$USER_SERVICE/api/orgs/$ORG_ID" \
    "GET" \
    "" \
    "-H 'Authorization: Bearer $TOKEN'"

# List organizations
test_endpoint "List Organizations" \
    "$USER_SERVICE/api/orgs" \
    "GET" \
    "" \
    "-H 'Authorization: Bearer $TOKEN'"

echo ""

# ==========================================
# 3. PROJECT TESTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}3️⃣  Testing Project Management${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Create project
project_response=$(curl -s -X POST "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"Test Project\",
        \"projectCode\": \"TEST${TIMESTAMP}\",
        \"description\": \"Test project for verification\",
        \"managerId\": $USER_ID
    }")

if echo "$project_response" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ Create Project${NC}"
    ((PASSED++))
    PROJECT_ID=$(echo "$project_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
else
    echo -e "${RED}✗ Create Project FAILED${NC}"
    echo "$project_response"
    ((FAILED++))
    exit 1
fi

# Get project
test_endpoint "Get Project" \
    "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects/$PROJECT_ID" \
    "GET" \
    "" \
    "-H 'Authorization: Bearer $TOKEN'"

# List projects
test_endpoint "List Projects" \
    "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects?page=0&size=20" \
    "GET" \
    "" \
    "-H 'Authorization: Bearer $TOKEN'"

echo ""

# ==========================================
# 4. TICKET TESTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}4️⃣  Testing Ticket Lifecycle${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Create ticket
ticket_response=$(curl -s -X POST "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects/$PROJECT_ID/tickets" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "title": "Test Ticket - Verification",
        "description": "This is a test ticket to verify functionality",
        "priority": "HIGH"
    }')

if echo "$ticket_response" | grep -q '"ticketNumber"'; then
    echo -e "${GREEN}✓ Create Ticket${NC}"
    ((PASSED++))
    TICKET_ID=$(echo "$ticket_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
    TICKET_NUMBER=$(echo "$ticket_response" | grep -o '"ticketNumber":"[^"]*' | cut -d'"' -f4)
    echo "  Ticket created: $TICKET_NUMBER"
else
    echo -e "${RED}✗ Create Ticket FAILED${NC}"
    ((FAILED++))
    exit 1
fi

# Get ticket
test_endpoint "Get Ticket" \
    "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects/$PROJECT_ID/tickets/$TICKET_ID" \
    "GET" \
    "" \
    "-H 'Authorization: Bearer $TOKEN'"

# List tickets
test_endpoint "List Tickets" \
    "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects/$PROJECT_ID/tickets?page=0&size=20" \
    "GET" \
    "" \
    "-H 'Authorization: Bearer $TOKEN'"

# Update ticket status
status_response=$(curl -s -X POST "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects/$PROJECT_ID/tickets/$TICKET_ID/status" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "status": "IN_PROGRESS",
        "comment": "Started working on this ticket"
    }')

if echo "$status_response" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ Update Ticket Status${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Update Ticket Status FAILED${NC}"
    ((FAILED++))
fi

echo ""

# ==========================================
# 5. COMMENT TESTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}5️⃣  Testing Comments & Collaboration${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Add comment
comment_response=$(curl -s -X POST "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects/$PROJECT_ID/tickets/$TICKET_ID/comments" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "text": "This is a test comment",
        "type": "COMMENT",
        "isInternal": false
    }')

if echo "$comment_response" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ Add Comment${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Add Comment FAILED${NC}"
    ((FAILED++))
fi

# Add work note (internal)
worknote_response=$(curl -s -X POST "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects/$PROJECT_ID/tickets/$TICKET_ID/comments" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "text": "Internal work note - not visible to requesters",
        "type": "WORK_NOTE",
        "isInternal": true
    }')

if echo "$worknote_response" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ Add Work Note (Internal)${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Add Work Note FAILED${NC}"
    ((FAILED++))
fi

# Get comments
test_endpoint "Get Comments" \
    "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects/$PROJECT_ID/tickets/$TICKET_ID/comments" \
    "GET" \
    "" \
    "-H 'Authorization: Bearer $TOKEN'"

echo ""

# ==========================================
# 6. HISTORY TESTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}6️⃣  Testing History & Audit Trail${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

history_response=$(curl -s "$ISSUE_SERVICE/api/orgs/$ORG_ID/projects/$PROJECT_ID/tickets/$TICKET_ID/history" \
    -H "Authorization: Bearer $TOKEN")

if echo "$history_response" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ Get Ticket History${NC}"
    ((PASSED++))
    history_count=$(echo "$history_response" | grep -o '"items":\[' | wc -l)
    echo "  History entries recorded"
else
    echo -e "${RED}✗ Get Ticket History FAILED${NC}"
    ((FAILED++))
fi

echo ""

# ==========================================
# 7. GROUP TESTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}7️⃣  Testing Support Groups${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Create L1 group
group_response=$(curl -s -X POST "$ISSUE_SERVICE/api/orgs/$ORG_ID/groups" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
        "name": "L1 Support Team",
        "description": "First level support",
        "level": "L1"
    }')

if echo "$group_response" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ Create Support Group${NC}"
    ((PASSED++))
    GROUP_ID=$(echo "$group_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
else
    echo -e "${RED}✗ Create Support Group FAILED${NC}"
    ((FAILED++))
fi

# List groups
test_endpoint "List Support Groups" \
    "$ISSUE_SERVICE/api/orgs/$ORG_ID/groups" \
    "GET" \
    "" \
    "-H 'Authorization: Bearer $TOKEN'"

echo ""

# ==========================================
# 8. CLIENT TESTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}8️⃣  Testing Client Management${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Create client
client_response=$(curl -s -X POST "$ISSUE_SERVICE/api/orgs/$ORG_ID/clients" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"Test Client\",
        \"email\": \"client${TIMESTAMP}@example.com\",
        \"phone\": \"+1234567890\",
        \"company\": \"Client Company Inc.\"
    }")

if echo "$client_response" | grep -q '"success":true'; then
    echo -e "${GREEN}✓ Create Client${NC}"
    ((PASSED++))
    CLIENT_ID=$(echo "$client_response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
else
    echo -e "${RED}✗ Create Client FAILED${NC}"
    ((FAILED++))
fi

# List clients
test_endpoint "List Clients" \
    "$ISSUE_SERVICE/api/orgs/$ORG_ID/clients?page=0&size=20" \
    "GET" \
    "" \
    "-H 'Authorization: Bearer $TOKEN'"

echo ""

# ==========================================
# 9. SECURITY TESTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}9️⃣  Testing Security Features${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Test without token (should fail)
no_auth_response=$(curl -s -w "\n%{http_code}" "$USER_SERVICE/api/users/me" 2>/dev/null)
no_auth_code=$(echo "$no_auth_response" | tail -n1)

if [ "$no_auth_code" = "401" ] || [ "$no_auth_code" = "403" ]; then
    echo -e "${GREEN}✓ Protected Endpoint Without Token (Correctly Denied)${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Security Issue: Endpoint accessible without token${NC}"
    ((FAILED++))
fi

# Test with invalid token (should fail)
invalid_token_response=$(curl -s -w "\n%{http_code}" "$USER_SERVICE/api/users/me" \
    -H "Authorization: Bearer invalid_token_12345" 2>/dev/null)
invalid_token_code=$(echo "$invalid_token_response" | tail -n1)

if [ "$invalid_token_code" = "401" ] || [ "$invalid_token_code" = "403" ]; then
    echo -e "${GREEN}✓ Invalid Token Rejected${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Security Issue: Invalid token accepted${NC}"
    ((FAILED++))
fi

echo ""

# ==========================================
# 10. RESPONSE FORMAT TESTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}🔟  Testing StandardResponse Format${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

# Check response has required fields
format_response=$(curl -s "$USER_SERVICE/api/orgs" -H "Authorization: Bearer $TOKEN")

if echo "$format_response" | grep -q '"success"'; then
    echo -e "${GREEN}✓ Response has 'success' field${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Response missing 'success' field${NC}"
    ((FAILED++))
fi

if echo "$format_response" | grep -q '"timestamp"'; then
    echo -e "${GREEN}✓ Response has 'timestamp' field${NC}"
    ((PASSED++))
else
    echo -e "${RED}✗ Response missing 'timestamp' field${NC}"
    ((FAILED++))
fi

echo ""

# ==========================================
# FINAL RESULTS
# ==========================================
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${BLUE}📊 Verification Results${NC}"
echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${GREEN}✓ Tests Passed: $PASSED${NC}"
echo -e "${RED}✗ Tests Failed: $FAILED${NC}"
echo ""

TOTAL=$((PASSED + FAILED))
SUCCESS_RATE=$((PASSED * 100 / TOTAL))

if [ $FAILED -eq 0 ]; then
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${GREEN}🎉 ALL TESTS PASSED! (100%)${NC}"
    echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo -e "${GREEN}✅ Platform is fully functional!${NC}"
    exit 0
else
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}⚠️  Some tests failed ($SUCCESS_RATE% success rate)${NC}"
    echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo ""
    echo -e "${YELLOW}Please check the failed tests above.${NC}"
    exit 1
fi
