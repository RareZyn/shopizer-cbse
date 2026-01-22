package com.shopizer.customer.repository;

import com.shopizer.common.entity.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Payment Repository Interface
 * FR-023: The system shall allow customers to view payment history
 */
public interface PaymentRepository {
    Optional<Payment> findById(Long id);
    List<Payment> findAll();
    List<Payment> findByCustomerId(Long customerId);
    Payment save(Payment payment);
    void delete(Payment payment);
    void deleteById(Long id);
}
