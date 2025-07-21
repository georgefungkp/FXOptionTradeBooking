package org.george.fxoptiontradebooking.exception;

import lombok.extern.slf4j.Slf4j;
import org.george.fxoptiontradebooking.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler for the FX Option Trade Booking application.
 * 
 * This class centralizes exception handling across the application, ensuring consistent
 * error responses, appropriate status codes, and proper logging for all exceptions.
 * It converts various exception types into standardized API responses that follow
 * the application's response format convention.
 * 
 * Key responsibilities:
 * - Map domain-specific exceptions to appropriate HTTP status codes
 * - Provide meaningful error messages for clients
 * - Log exceptions at appropriate severity levels
 * - Shield internal implementation details from API clients
 * 
 * This handler is crucial for maintaining a consistent API contract and ensuring
 * proper error handling in a financial application where accuracy and auditability
 * are essential.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Handles trade not found exceptions.
     * 
     * This occurs when a client requests a trade that doesn't exist in the system,
     * typically through an invalid ID or reference. The appropriate response is
     * a 404 NOT FOUND status with details about the missing trade.
     * 
     * @param ex The trade not found exception
     * @return A response entity with 404 status and error details
     */
    @ExceptionHandler(TradeNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleTradeNotFoundException(TradeNotFoundException ex) {
        log.warn("Trade not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Trade not found", ex.getMessage()));
    }

    /**
     * Handles business validation exceptions.
     * 
     * These exceptions occur when a client request fails to meet business rules
     * or validation criteria. Common causes include invalid trade data, prohibited
     * status transitions, or constraint violations. The appropriate response is
     * a 400 BAD REQUEST status with details about the validation failure.
     * 
     * @param ex The business validation exception
     * @return A response entity with 400 status and validation error details
     */
    @ExceptionHandler(BusinessValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessValidationException(BusinessValidationException ex) {
        log.warn("Business validation error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation failed", ex.getMessage()));
    }

    /**
     * Handles resource not found exceptions from Spring MVC.
     * 
     * This occurs when a client requests a URL or resource that doesn't exist
     * in the application, typically due to an invalid endpoint path. The appropriate
     * response is a 404 NOT FOUND status with a generic message about the missing resource.
     * 
     * @param ex The Spring MVC no resource found exception
     * @return A response entity with 404 status and error details
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException(NoResourceFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Resource not found", "The requested resource could not be found"));
    }

    /**
     * Fallback handler for all unhandled exceptions.
     * 
     * This catch-all handler ensures that any unexpected exceptions are properly
     * logged and converted to a consistent API response format. It shields internal
     * error details from clients and logs full stack traces for debugging.
     * 
     * Note that this is logged at ERROR level, unlike most other exceptions which
     * are logged at WARN level, as unhandled exceptions typically indicate a system
     * error rather than a client error.
     * 
     * @param ex The unhandled exception
     * @return A response entity with 500 status and a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Internal server error", "An unexpected error occurred"));
    }
}