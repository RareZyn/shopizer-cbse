package com.shopizer.springboot.cart.dto;

/**
 * Cart Request DTO
 * Data Transfer Object for creating a new shopping cart.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: The system shall allow customers to manage cart items (Add, View, Update, Remove)
 * - FR-009: The system shall persist cart data for logged-in customers
 *
 * USE CASES:
 * - UC04: Add to Cart - Cart is auto-created if none exists when customer adds first item
 * - Supports both authenticated customers (customerId) and guest users (sessionId)
 *
 * ROLE IN ARCHITECTURE:
 * - INPUT DTO at the Controller layer (CartController)
 * - Used by POST /api/v1/cart endpoint for explicit cart creation
 * - Processed by CartService.createCart()
 * - Supports anonymous cart management (FR-009: cart persistence)
 *
 * BUSINESS LOGIC:
 * - customerId: For logged-in customers (persistent cart)
 * - sessionId: For guest users (session-based cart)
 * - Either customerId OR sessionId should be provided (not both)
 */
public class CartRequest {

    private Long customerId;
    private String sessionId;

    // Constructors
    public CartRequest() {}

    public CartRequest(Long customerId, String sessionId) {
        this.customerId = customerId;
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
