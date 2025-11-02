package com.juvarya.commonservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;

/**
 * Standardized API response wrapper
 * Usage: 
 *   - StandardResponse.single(data)
 *   - StandardResponse.list(items)
 *   - StandardResponse.page(pageData)
 *   - StandardResponse.error(message, code)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private List<T> items;
    private PageInfo pageInfo;
    private String errorCode;
    private Instant timestamp;
    
    // Single object response
    public static <T> StandardResponse<T> single(T data) {
        StandardResponse<T> response = new StandardResponse<>();
        response.setSuccess(true);
        response.setMessage("Success");
        response.setData(data);
        response.setTimestamp(Instant.now());
        return response;
    }
    
    // Single object with custom message
    public static <T> StandardResponse<T> single(T data, String message) {
        StandardResponse<T> response = new StandardResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(Instant.now());
        return response;
    }
    
    // List response
    public static <T> StandardResponse<T> list(List<T> items) {
        StandardResponse<T> response = new StandardResponse<>();
        response.setSuccess(true);
        response.setMessage("Success");
        response.setItems(items);
        response.setTimestamp(Instant.now());
        return response;
    }
    
    // List response with custom message
    public static <T> StandardResponse<T> list(List<T> items, String message) {
        StandardResponse<T> response = new StandardResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setItems(items);
        response.setTimestamp(Instant.now());
        return response;
    }
    
    // Paginated response
    public static <T> StandardResponse<T> page(Page<T> page) {
        StandardResponse<T> response = new StandardResponse<>();
        response.setSuccess(true);
        response.setMessage("Success");
        response.setItems(page.getContent());
        response.setPageInfo(new PageInfo(
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.isFirst(),
            page.isLast()
        ));
        response.setTimestamp(Instant.now());
        return response;
    }
    
    // Error response
    public static <T> StandardResponse<T> error(String message, String errorCode) {
        StandardResponse<T> response = new StandardResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrorCode(errorCode);
        response.setTimestamp(Instant.now());
        return response;
    }
    
    // Error response with default code
    public static <T> StandardResponse<T> error(String message) {
        return error(message, "INTERNAL_ERROR");
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private int currentPage;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;
    }
}
