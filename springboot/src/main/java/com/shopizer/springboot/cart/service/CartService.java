package com.shopizer.springboot.cart.service;

import com.shopizer.springboot.cart.entity.Cart;
import java.util.List;
import java.util.Optional;

/**
 * Cart Service Interface
 * FR-006: Cart item management
 * FR-007: Calculate cart total
 */
public interface CartService {

    Cart createCart(Cart cart);
    Optional<Cart> getCartById(Long id);
    Optional<Cart> getCartByCustomerId(Long customerId);
    List<Cart> getAllCarts();
    void deleteCart(Long id);
}
