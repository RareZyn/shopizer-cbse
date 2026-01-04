package com.shopizer.springboot.cart.repository;

import com.shopizer.springboot.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Cart Repository Interface
 * FR-006: Cart item management
 * FR-009: Persist cart data for logged-in customers
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomerId(Long customerId);

    Optional<Cart> findBySessionId(String sessionId);
}
