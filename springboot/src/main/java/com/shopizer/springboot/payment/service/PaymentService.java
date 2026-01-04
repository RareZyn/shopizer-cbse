package com.shopizer.springboot.payment.service;

import com.shopizer.springboot.payment.entity.Payment;
import java.util.List;
import java.util.Optional;

/**
 * Payment Service Interface
 * FR-019 to FR-023: Payment management
 */
public interface PaymentService {

    Payment createPayment(Payment payment);
    Optional<Payment> getPaymentById(Long id);
    List<Payment> getPaymentsByOrderId(Long orderId);
    List<Payment> getPaymentsByCustomerId(Long customerId);
    List<Payment> getAllPayments();
    Payment updatePaymentStatus(Long id, String status);
}
