package com.shopizer.cart.api;

import com.shopizer.cart.dto.*;
import com.shopizer.common.entity.Cart;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


/**
 * OSGI Service Interface for Cart Module
 * Provides shopping cart operations (FR-006 to FR-009)
 */
public interface CartService {

    // ========== Cart Management ==========

    /**
     * Create a new cart
     * @param cart Cart entity to create
     * @return Created cart
     */
    Cart createCart(Cart cart);

    /**
     * Get all carts (Admin function)
     * @return List of all carts
     */
    List<Cart> getAllCarts();

    /**
     * Get cart by ID
     * @param id Cart ID
     * @return Cart if found
     */
    Optional<Cart> getCartById(Long id);

    /**
     * Delete cart by ID
     * @param id Cart ID
     */
    void deleteCartById(Long id);

    /**
     * Clear all items from cart by cart ID
     * @param cartId Cart ID
     */
    void clearCartById(Long cartId);

    /**
     * Get cart by customer ID
     * @param customerId Customer ID
     * @return CartResponse for the customer
     */
    CartResponse getCartByCustomerId(Long customerId);

    /**
     * Get cart total by cart ID
     * @param cartId Cart ID
     * @return Total amount
     */
    BigDecimal getCartTotalById(Long cartId);

    // ========== Cart Item Management by Cart ID ==========

    /**
     * Add item to cart by cart ID (FR-006)
     * @param cartId Cart ID
     * @param request Cart item request with product details
     * @return Updated cart response
     */
    CartResponse addItemToCart(Long cartId, CartItemRequest request);

    /**
     * Update cart item quantity by cart ID
     * @param cartId Cart ID
     * @param itemId Cart item ID
     * @param quantity New quantity
     * @return Updated cart response
     */
    CartResponse updateCartItemById(Long cartId, Long itemId, Integer quantity);

    /**
     * Remove item from cart by cart ID
     * @param cartId Cart ID
     * @param itemId Cart item ID
     */
    void removeItemFromCartById(Long cartId, Long itemId);

    // ========== Customer-centric Operations ==========

    /**
     * Add item to cart by customer ID (FR-006)
     * @param customerId Customer ID
     * @param request Cart item request with product details
     * @return Updated cart response
     */
    CartResponse addToCart(Long customerId, CartItemRequest request);

    /**
     * Add item to customer cart (alias for addToCart)
     * @param customerId Customer ID
     * @param request Cart item request with product details
     * @return Updated cart response
     */
    CartResponse addItemToCustomerCart(Long customerId, CartItemRequest request);

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
