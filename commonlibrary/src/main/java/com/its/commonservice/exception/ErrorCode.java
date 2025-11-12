package com.its.commonservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Centralized error code enum
 * Each error has: code, message, and HTTP status
 */
@Getter
public enum ErrorCode {
    
    // Authentication & Authorization (1000-1999)
    UNAUTHORIZED("AUTH_1001", "Authentication required", HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS("AUTH_1002", "Invalid username or password", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH_1003", "Authentication token has expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID("AUTH_1004", "Invalid authentication token", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("AUTH_1005", "Access forbidden - insufficient permissions", HttpStatus.FORBIDDEN),
    USER_ALREADY_EXISTS("AUTH_1006", "User already exists with this email or username", HttpStatus.CONFLICT),

    // User Management (2000-2999)
    USER_NOT_FOUND("USER_2001", "User not found", HttpStatus.NOT_FOUND),
    USER_INACTIVE("USER_2002", "User account is inactive", HttpStatus.FORBIDDEN),
    USER_NOT_IN_ORG("USER_2003", "User is not a member of this organization", HttpStatus.FORBIDDEN),
    INVALID_USER_ROLE("USER_2004", "Invalid user role", HttpStatus.BAD_REQUEST),

    // Organization Management (3000-3999)
    ORG_NOT_FOUND("ORG_3001", "Organization not found", HttpStatus.NOT_FOUND),
    ORG_ALREADY_EXISTS("ORG_3002", "Organization already exists", HttpStatus.CONFLICT),
    ORG_CODE_TAKEN("ORG_3003", "Organization code is already taken", HttpStatus.CONFLICT),
    DEPARTMENT_NOT_FOUND("ORG_3004", "Department not found", HttpStatus.NOT_FOUND),
    NOT_ORG_ADMIN("ORG_3005", "Only organization admins can perform this action", HttpStatus.FORBIDDEN),
    USER_ALREADY_IN_ORG("ORG_3006", "User is already a member of this organization", HttpStatus.CONFLICT),

    // Project Management (4000-4999)
    PROJECT_NOT_FOUND("PROJ_4001", "Project not found", HttpStatus.NOT_FOUND),
    PROJECT_CODE_TAKEN("PROJ_4002", "Project code is already taken", HttpStatus.CONFLICT),
    PROJECT_INACTIVE("PROJ_4003", "Project is inactive", HttpStatus.BAD_REQUEST),
    USER_NOT_IN_PROJECT("PROJ_4004", "User is not a member of this project", HttpStatus.FORBIDDEN),

    // Client Management (5000-5999)
    CLIENT_NOT_FOUND("CLIENT_5001", "Client not found", HttpStatus.NOT_FOUND),
    CLIENT_EMAIL_EXISTS("CLIENT_5002", "Client with this email already exists", HttpStatus.CONFLICT),

    // Group Management (6000-6999)
    GROUP_NOT_FOUND("GROUP_6001", "Group not found", HttpStatus.NOT_FOUND),
    GROUP_NAME_EXISTS("GROUP_6002", "Group with this name already exists", HttpStatus.CONFLICT),
    INVALID_GROUP_LEVEL("GROUP_6003", "Invalid group level. Must be L1, L2, or L3", HttpStatus.BAD_REQUEST),
    USER_ALREADY_IN_GROUP("GROUP_6004", "User is already a member of this group", HttpStatus.CONFLICT),

    // Ticket Management (7000-7999)
    TICKET_NOT_FOUND("TICKET_7001", "Ticket not found", HttpStatus.NOT_FOUND),
    TICKET_NUMBER_EXISTS("TICKET_7002", "Ticket number already exists", HttpStatus.CONFLICT),
    INVALID_TICKET_STATUS("TICKET_7003", "Invalid ticket status", HttpStatus.BAD_REQUEST),
    INVALID_TICKET_PRIORITY("TICKET_7004", "Invalid ticket priority", HttpStatus.BAD_REQUEST),
    INVALID_ASSIGNMENT("TICKET_7005", "Invalid assignment - target not found", HttpStatus.BAD_REQUEST),
    CATEGORY_NOT_FOUND("TICKET_7006", "Category not found", HttpStatus.NOT_FOUND),
    SUB_CATEGORY_NOT_FOUND("TICKET_7007", "Sub-category not found", HttpStatus.NOT_FOUND),

    // Comment & History (8000-8999)
    COMMENT_NOT_FOUND("COMMENT_8001", "Comment not found", HttpStatus.NOT_FOUND),
    COMMENT_EMPTY("COMMENT_8002", "Comment text cannot be empty", HttpStatus.BAD_REQUEST),

    // File/Attachment (9000-9999)
    FILE_NOT_FOUND("FILE_9001", "File not found", HttpStatus.NOT_FOUND),
    FILE_TOO_LARGE("FILE_9002", "File size exceeds maximum allowed limit", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED("FILE_9003", "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_FILE_TYPE("FILE_9004", "Invalid file type", HttpStatus.BAD_REQUEST),
    FILE_ACCESS_DENIED("FILE_9005", "Access to file denied", HttpStatus.FORBIDDEN),

    // Validation (10000-10999)
    INVALID_INPUT("VAL_10001", "Invalid input data", HttpStatus.BAD_REQUEST),
    MISSING_REQUIRED_FIELD("VAL_10002", "Required field is missing", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT("VAL_10003", "Invalid email format", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("VAL_10004", "Invalid date range", HttpStatus.BAD_REQUEST),

    // System/Internal (11000-11999)
    INTERNAL_ERROR("SYS_11001", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("SYS_11002", "Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    DATABASE_ERROR("SYS_11003", "Database error occurred", HttpStatus.INTERNAL_SERVER_ERROR),
    RESOURCE_NOT_FOUND("SYS_11004", "Resource not found", HttpStatus.NOT_FOUND),
    TICKET_ALREADY_ASSIGNED("TICKET_7008", "Ticket already assigned", HttpStatus.BAD_REQUEST),
    TICKET_ALREADY_CLOSED("TICKET_7009", "Ticket already closed", HttpStatus.BAD_REQUEST),
    GROUP_NOT_FOUND_FOR_PROJECT("GROUP_6006", "Group not found for project", HttpStatus.NOT_FOUND),
    GROUP_LEAD_NOT_ASSIGNED("GROUP_6007", "Group lead not assigned", HttpStatus.BAD_REQUEST),
    BUSINESS_VALIDATION_FAILED("VAL_10005", "Business validation failed", HttpStatus.BAD_REQUEST),
    DUPLICATE_SUBCATEGORY("SUBCAT_10006", "Subcategory already exists", HttpStatus.CONFLICT),
    SUBCATEGORY_NOT_FOUND("SUBCAT_10007", "Subcategory not found", HttpStatus.NOT_FOUND),
    DUPLICATE_ENTRY("ENT_10008", "Entry already exists", HttpStatus.CONFLICT),
    TECH_STACK_NOT_FOUND("TECH_10009", "Tech stack not found", HttpStatus.NOT_FOUND),
    PROJECT_ALREADY_REGISTERED("PROJ_10010", "Project already registered", HttpStatus.CONFLICT),
    BUSINESS_NOT_FOUND("BUS_10011", "Business not found", HttpStatus.NOT_FOUND),
    USER_CREATION_FAILED("USER_10012", "User creation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ASSIGNMENT_NOT_FOUND("ASSIGN_10013", "Assignment not found", HttpStatus.NOT_FOUND),
    ASSIGNMENT_ALREADY_ACTIVE("ASSIGN_10014", "Assignment already active", HttpStatus.BAD_REQUEST),
    ASSIGNMENT_ALREADY_INACTIVE("ASSIGN_10015", "Assignment already inactive", HttpStatus.BAD_REQUEST),
    INVALID_CLIENT_ROLE("CLIENT_10016", "Invalid client role", HttpStatus.BAD_REQUEST),
    INVALID_TARGET_TYPE("TARGET_10017", "Invalid target type", HttpStatus.BAD_REQUEST),
    USER_ALREADY_REGISTERED("USER_10018", "User already registered", HttpStatus.CONFLICT),
    GROUP_ALREADY_EXISTS_FOR_PRIORITY("GROUP_6008", "Group already exists for priority", HttpStatus.CONFLICT),
    DUPLICATE_GROUP_NAME("GROUP_6009", "Group name already exists", HttpStatus.CONFLICT),
    DUPLICATE_CATEGORY("CAT_10010", "Category already exists", HttpStatus.CONFLICT),
    PROJECT_CODE_GENERATION_FAILED("PROJ_10011", "Project code generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
