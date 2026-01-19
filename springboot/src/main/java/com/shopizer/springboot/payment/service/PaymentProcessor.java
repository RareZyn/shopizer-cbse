package com.shopizer.springboot.payment.service;

import java.math.BigDecimal;

import com.shopizer.springboot.order.entity.Order;

/**
 * CBSE Service Interface
 * This acts as the Service Contract between the Order and Payment modules.
 */
public interface PaymentProcessor {
    /**
     * Processes payment for a given order
     * @return transactionId if successful, null otherwise
     */
    String process(Order order, BigDecimal amount);
    
    /**
     * Returns the name of the current active payment component
     */
    String getComponentName();
}