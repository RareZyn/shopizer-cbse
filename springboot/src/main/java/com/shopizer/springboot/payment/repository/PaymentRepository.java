package com.shopizer.springboot.payment.repository;

import com.shopizer.springboot.payment.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Payment Repository Interface
 * FR-020: Payment gateway integration
 * FR-022: Store payment history
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByCustomerId(Long customerId);
}
