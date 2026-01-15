package com.shopizer.springboot.cart.repository;

import com.shopizer.springboot.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Cart Item Repository Interface
 * Data Access Layer for CartItem entity using Spring Data JPA.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: The system shall allow customers to manage cart items (Add, View, Update, Remove)
 * - FR-009: The system shall persist cart data for logged-in customers
 *
 * USE CASES:
 * - UC04: Add to Cart - Uses findByCartIdAndProductId() to check for duplicates before adding
 * - UC05: View Cart - Uses findByCartId() to retrieve all items for display
 * - UC06: Update Cart - Uses findById(), save(), delete() for modifications
 *                      - Uses deleteByCartId() to clear entire cart
 *
 * ROLE IN ARCHITECTURE:
 * - REPOSITORY LAYER (Data Access)
 * - Extends Spring Data JPA's JpaRepository for standard CRUD operations
 * - Provides custom query methods for cart-specific operations
 * - Used exclusively by CartServiceImpl (Service Layer)
 * - Abstracts database access from business logic
 *
 * CUSTOM QUERY METHODS:
 * 1. findByCartId() - Retrieve all items in a specific cart (UC05)
 * 2. findByCartIdAndProductId() - Check if product already in cart to prevent duplicates (UC04)
 * 3. deleteByCartId() - Clear all items from cart (UC06)
 * 4. countByCartId() - Get total number of items in cart for FR-007
 *
 * DATABASE OPERATIONS:
 * - Automatic transaction management via @Transactional in Service layer
 * - JPA entity relationships: CartItem @ManyToOne Cart
 * - Cascade operations handled at entity level
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all cart items for a specific cart
     * Used in UC05 (View Cart) to retrieve all items for display
     */
    List<CartItem> findByCartId(Long cartId);

    /**
     * Find a specific item in a cart by product ID
     * Used in UC04 (Add to Cart) to check for duplicate products
     * Prevents adding same product twice - instead updates quantity
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Delete all items from a specific cart
     * Used in UC06 (Update Cart) for "Clear Cart" operation
     */
    void deleteByCartId(Long cartId);

    /**
     * Count items in a cart
     * Used for FR-007 to calculate itemCount in CartResponse
     */
    int countByCartId(Long cartId);
}
