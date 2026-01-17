package com.shopizer.springboot.cart.service;

import com.shopizer.springboot.cart.dto.CartItemRequest;
import com.shopizer.springboot.cart.dto.CartResponse;
import com.shopizer.springboot.cart.entity.Cart;
import com.shopizer.springboot.cart.entity.CartItem;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Cart Service Interface
 * Business logic contract defining all cart and cart item operations.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: Cart item management (Add, View, Update, Remove)
 * - FR-007: Calculate and display cart total automatically
 * - FR-008: Validate product availability before adding to cart
 * - FR-009: Persist cart data for logged-in customers
 *
 * USE CASES IMPLEMENTED:
 *
 * UC04: Add to Cart
 * - addItemToCart(cartId, request) - Add item by cart ID
 * - addItemToCustomerCart(customerId, request) - Add item by customer ID
 * - Flow: Validate product → Check stock → Check duplicate → Add or update quantity
 * - Implements FR-008 product validation
 *
 * UC05: View Cart
 * - getCartResponse(cartId) - Get complete cart details
 * - getCartResponseByCustomerId(customerId) - Get cart by customer
 * - Returns CartResponse DTO with items, total, itemCount
 * - Empty cart handling: returns items=[], total=0, itemCount=0
 *
 * UC06: Update Cart
 * - updateCartItemQuantity(cartId, itemId, quantity) - Change item quantity
 * - removeItemFromCart(cartId, itemId) - Remove single item
 * - clearCart(cartId) - Remove all items
 * - Re-validates stock availability on quantity updates (FR-008)
 *
 * ROLE IN ARCHITECTURE:
 * - SERVICE LAYER (Business Logic)
 * - Interface defines contracts for all cart operations
 * - Implemented by CartServiceImpl with full business logic
 * - Used by CartController (Presentation Layer)
 * - Depends on Repository Layer for data persistence
 *
 * METHOD CATEGORIES:
 * 1. Cart Management (8 methods) - Create, read, delete carts
 * 2. Cart Item Management (5 methods) - Add, update, remove items
 * 3. Cart Calculations (3 methods) - Calculate totals, prepare responses
 * 4. Cart Merge (1 method) - Merge guest cart to customer cart
 *
 * TOTAL METHODS: 19 (extended from original 5)
 *
 * CROSS-MODULE INTEGRATION:
 * - Integrates with Catalog module for product validation (FR-008)
 * - Uses ProductRepository to validate product existence and stock
 */
public interface CartService {

    // ========== Cart Management ==========

    /**
     * Create a new cart for a customer or session
     * FR-009: Persist cart data
     */
    Cart createCart(Cart cart);

    /**
     * Get cart by ID
     * FR-006: View cart
     */
    Optional<Cart> getCartById(Long id);

    /**
     * Get or create cart for a customer
     * FR-009: Persist cart data for logged-in customers
     */
    Cart getOrCreateCartByCustomerId(Long customerId);

    /**
     * Get or create cart for a session
     * For guest users
     */
    Cart getOrCreateCartBySessionId(String sessionId);

    /**
     * Get cart by customer ID
     * FR-006: View cart
     */
    Optional<Cart> getCartByCustomerId(Long customerId);

    /**
     * Get all carts (admin function)
     */
    List<Cart> getAllCarts();

    /**
     * Delete/clear cart
     * FR-006: Manage cart items (Remove all)
     */
    void deleteCart(Long id);

    /**
     * Clear all items from cart but keep cart
     * FR-006: Manage cart items (Remove all)
     */
    void clearCart(Long cartId);

    // ========== Cart Item Management ==========

    /**
     * Add item to cart with product validation
     * UC04: Add to Cart
     * FR-006: Add items to cart
     * FR-008: Validate product availability
     */
    CartItem addItemToCart(Long cartId, CartItemRequest request);

    /**
     * Add item to customer's cart (by customer ID)
     * UC04: Add to Cart
     * FR-006: Add items to cart
     * FR-008: Validate product availability
     */
    CartItem addItemToCustomerCart(Long customerId, CartItemRequest request);

    /**
     * Update cart item quantity
     * UC06: Update Cart
     * FR-006: Update cart items
     * FR-008: Validate product availability
     */
    CartItem updateCartItemQuantity(Long cartId, Long itemId, Integer quantity);

    /**
     * Remove item from cart
     * UC06: Update Cart - Remove item
     * FR-006: Remove items from cart
     */
    void removeItemFromCart(Long cartId, Long itemId);

    /**
     * Get cart item by ID
     * FR-006: View cart items
     */
    Optional<CartItem> getCartItemById(Long itemId);

    // ========== Cart Calculations ==========

    /**
     * Calculate and get cart total
     * FR-007: Calculate and display cart total automatically
     */
    BigDecimal getCartTotal(Long cartId);

    /**
     * Get cart with all details as response DTO
     * UC05: View Cart
     * FR-006: View cart
     * FR-007: Display cart total
     */
    CartResponse getCartResponse(Long cartId);

    /**
     * Get cart response by customer ID
     * UC05: View Cart
     * FR-006: View cart
     * FR-007: Display cart total
     */
    CartResponse getCartResponseByCustomerId(Long customerId);

    // ========== Cart Merge (Guest to Customer) ==========

    /**
     * Merge guest cart (session) with customer cart after login
     * FR-009: Persist cart data for logged-in customers
     */
    Cart mergeGuestCartToCustomer(String sessionId, Long customerId);
}
