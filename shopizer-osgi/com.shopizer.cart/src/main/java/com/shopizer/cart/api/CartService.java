package com.shopizer.cart.api;

import com.shopizer.cart.dto.*;
import java.math.BigDecimal;


/**
 * OSGI Service Interface for Cart Module
 * Provides shopping cart operations (FR-006 to FR-009)
 */
public interface CartService {

    /**
     * Add item to cart (FR-006)
     * @param customerId Customer ID
     * @param request Cart item request with product details
     * @return Updated cart response
     */
    CartResponse addToCart(Long customerId, CartItemRequest request);

    /**
     * View cart contents (FR-007)
     * @param customerId Customer ID
     * @return Current cart with all items
     */
    CartResponse viewCart(Long customerId);

    /**
     * Update cart item quantity (FR-008)
     * @param customerId Customer ID
     * @param itemId Cart item ID
     * @param quantity New quantity
     * @return Updated cart response
     */
    CartResponse updateCartItem(Long customerId, Long itemId, Integer quantity);

    /**
     * Remove item from cart (FR-008)
     * @param customerId Customer ID
     * @param itemId Cart item ID
     */
    void removeFromCart(Long customerId, Long itemId);

    /**
     * Clear entire cart
     * @param customerId Customer ID
     */
    void clearCart(Long customerId);

    /**
     * Calculate cart total (FR-009)
     * @param customerId Customer ID
     * @return Total amount including all items
     */
    BigDecimal calculateTotal(Long customerId);

    /**
     * Validate cart before checkout (FR-009)
     * @param customerId Customer ID
     * @return Validation result with any errors
     */
    CartValidationResponse validateCart(Long customerId);

    /**
     * Get cart item count
     * @param customerId Customer ID
     * @return Number of items in cart
     */
    Integer getCartItemCount(Long customerId);

    /**
     * Merge anonymous cart with customer cart (after login)
     * @param anonymousCartId Anonymous cart ID
     * @param customerId Customer ID
     * @return Merged cart
     */
    CartResponse mergeCart(Long anonymousCartId, Long customerId);
}
