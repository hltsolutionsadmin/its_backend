package com.juvarya.commonservice.exception;

import lombok.Getter;

/**
 * Custom exception that accepts ErrorCode
 * Never expose raw exceptions - always use this for business logic errors
 */
@Getter
public class HltCustomerException extends RuntimeException {
    
    private final ErrorCode errorCode;
    private final String customMessage;
    
    public HltCustomerException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = null;
    }
    
    public HltCustomerException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
    
    public HltCustomerException(ErrorCode errorCode, String customMessage, Throwable cause) {
        super(customMessage, cause);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
    
    public String getEffectiveMessage() {
        return customMessage != null ? customMessage : errorCode.getMessage();
    }
}
