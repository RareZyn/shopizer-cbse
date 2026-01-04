package com.shopizer.springboot.order.repository;

import com.shopizer.springboot.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Order Repository Interface
 * FR-010: Place orders
 * FR-012: Track order status
 * FR-013: View order history
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    List<Order> findByCustomerId(Long customerId);

    List<Order> findByStatus(String status);
}
