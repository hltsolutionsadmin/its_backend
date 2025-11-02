package com.juvarya.commonservice.exception;

import com.juvarya.commonservice.dto.StandardResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

/**
 * Global exception handler
 * Converts all exceptions to StandardResponse.error()
 * Never exposes raw exceptions or stack traces to clients
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle custom business exceptions
     */
    @ExceptionHandler(HltCustomerException.class)
    public ResponseEntity<StandardResponse<Void>> handleCustomerException(HltCustomerException ex) {
        log.error("Business exception: {} - {}", ex.getErrorCode().getCode(), ex.getEffectiveMessage(), ex);
        
        StandardResponse<Void> response = StandardResponse.error(
            ex.getEffectiveMessage(),
            ex.getErrorCode().getCode()
        );
        
        return ResponseEntity
            .status(ex.getErrorCode().getHttpStatus())
            .body(response);
    }
    
    /**
     * Handle Spring Security authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<StandardResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication exception: {}", ex.getMessage(), ex);
        
        StandardResponse<Void> response = StandardResponse.error(
            ex.getMessage(),
            ErrorCode.UNAUTHORIZED.getCode()
        );
        
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(response);
    }
    
    /**
     * Handle Spring Security access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied: {}", ex.getMessage(), ex);
        
        StandardResponse<Void> response = StandardResponse.error(
            "Access denied - insufficient permissions",
            ErrorCode.FORBIDDEN.getCode()
        );
        
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(response);
    }
    
    /**
     * Handle validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        log.error("Validation exception: {}", errorMessage);
        
        StandardResponse<Void> response = StandardResponse.error(
            errorMessage,
            ErrorCode.INVALID_INPUT.getCode()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }
    
    /**
     * Handle type mismatch errors
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<StandardResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'", 
            ex.getValue(), ex.getName());
        
        log.error("Type mismatch: {}", errorMessage, ex);
        
        StandardResponse<Void> response = StandardResponse.error(
            errorMessage,
            ErrorCode.INVALID_INPUT.getCode()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }
    
    /**
     * Handle file upload size exceeded
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<StandardResponse<Void>> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.error("File size exceeded", ex);
        
        StandardResponse<Void> response = StandardResponse.error(
            ErrorCode.FILE_TOO_LARGE.getMessage(),
            ErrorCode.FILE_TOO_LARGE.getCode()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }
    
    /**
     * Handle all other unexpected exceptions
     * Never expose stack traces to clients
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected exception occurred", ex);
        
        // Don't expose internal error details to clients
        StandardResponse<Void> response = StandardResponse.error(
            "An unexpected error occurred. Please try again later.",
            ErrorCode.INTERNAL_ERROR.getCode()
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }
}
