package com.shopizer.springboot.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Cart Item Request DTO
 * Data Transfer Object used for adding items to cart and updating cart item quantity.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: The system shall allow customers to manage cart items (Add, View, Update, Remove)
 * - FR-008: The system shall validate product availability before adding to cart
 *
 * USE CASES:
 * - UC04: Add to Cart - This DTO captures the product ID and quantity when customer adds item to cart
 * - UC06: Update Cart - This DTO is used when customer changes the quantity of a cart item
 *
 * ROLE IN ARCHITECTURE:
 * - INPUT DTO at the Controller layer (CartController)
 * - Validated via Jakarta Bean Validation (@NotNull, @Min annotations)
 * - Processed by CartService.addItemToCart() and CartService.updateCartItemQuantity()
 * - Ensures data integrity before business logic execution
 *
 * VALIDATION RULES:
 * - productId: Required (cannot be null)
 * - quantity: Required and must be at least 1
 */
public class CartItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Constructors
    public CartItemRequest() {}

    public CartItemRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
