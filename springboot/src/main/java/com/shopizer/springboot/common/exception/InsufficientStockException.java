package com.shopizer.springboot.common.exception;

/**
 * Exception thrown when product stock is insufficient
 * Related to FR-005: Stock level tracking
 * Related to FR-010: Product availability validation
 */
public class InsufficientStockException extends RuntimeException {

    private Long productId;
    private Integer requestedQuantity;
    private Integer availableQuantity;

    /**
     * Constructor with message only
     */
    public InsufficientStockException(String message) {
        super(message);
    }

    /**
     * Constructor with stock details
     */
    public InsufficientStockException(Long productId, Integer requestedQuantity, Integer availableQuantity) {
        super(String.format("Insufficient stock for product ID %d. Requested: %d, Available: %d",
                productId, requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }

    /**
     * Constructor with message and cause
     */
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}
