package com.shopizer.springboot.common.exception;

/**
 * Exception thrown when product stock is insufficient
 * Related to FR-005: Stock level tracking
 * Related to FR-008: Product availability validation
 */
public class InsufficientStockException extends RuntimeException {

    public InsufficientStockException() {
        super();
    }

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }
}
