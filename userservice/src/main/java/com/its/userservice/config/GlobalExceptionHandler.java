//package com.its.userservice.config;
//
//import com.its.common.exception.ErrorResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.context.request.WebRequest;
//
//import java.time.Instant;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, WebRequest request) {
//        List<String> details = ex.getBindingResult().getFieldErrors().stream()
//                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
//                .collect(Collectors.toList());
//        ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
//                "Validation Failed", "Invalid request", request.getDescription(false), details);
//        return ResponseEntity.badRequest().body(body);
//    }
//
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
//        ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.BAD_REQUEST.value(),
//                "Bad Request", ex.getMessage(), request.getDescription(false), null);
//        return ResponseEntity.badRequest().body(body);
//    }
//
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, WebRequest request) {
//        ErrorResponse body = new ErrorResponse(Instant.now(), HttpStatus.INTERNAL_SERVER_ERROR.value(),
//                "Internal Server Error", ex.getMessage(), request.getDescription(false), null);
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
//    }
//}
