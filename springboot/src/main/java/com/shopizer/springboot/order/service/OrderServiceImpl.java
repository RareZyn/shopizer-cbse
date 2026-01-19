package com.shopizer.springboot.order.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopizer.springboot.order.entity.Order;
import com.shopizer.springboot.order.repository.OrderRepository;
import com.shopizer.springboot.payment.service.PaymentProcessor;

/**
 * Order Service Implementation
 * Integrated with Payment Module for CBSE requirements
 * FR-010: Place orders (triggers payment)
 * FR-012: Track order status
 * FR-013: View order history
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    // This field represents our pluggable OSGi component
    @Autowired
    private PaymentProcessor paymentProcessor;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * FR-010: Place Order
     * This method orchestrates the order creation and triggers the 
     * external payment component.
     */
    @Override
    @Transactional
    public Order createOrder(Order order) {
        // 1. Initial validation
        if (order.getTotalAmount() == null || order.getCustomerId() == null) {
            throw new RuntimeException("Invalid order data: Missing amount or customer ID");
        }

        // 2. Set initial status
        order.setStatus("PROCESSING");

        // 3. CBSE Logic: Delegate to the Payment Component (OSGi/Spring Bridge)
        // The Order module doesn't care how payment works, only that it gets a result.
        try {
            String transactionId = paymentProcessor.process(order, order.getTotalAmount());
            
            if (transactionId != null && !transactionId.isEmpty()) {
                // Payment success: Update status
                order.setStatus("PAID");
                System.out.println("Payment successful for Order " + order.getOrderNumber() + ". TXN ID: " + transactionId);
            } else {
                order.setStatus("PAYMENT_FAILED");
            }
        } catch (Exception e) {
            // If the Payment bundle is missing or fails
            order.setStatus("PENDING_PAYMENT_RETRY");
            System.err.println("Error: Payment component unreachable or failed: " + e.getMessage());
        }

        // 4. Persist the finalized order to the database
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByCustomerId(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Cannot delete: Order not found with ID: " + id);
        }
        orderRepository.deleteById(id);
    }
}