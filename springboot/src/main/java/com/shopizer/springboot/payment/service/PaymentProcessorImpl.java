package com.shopizer.springboot.payment.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.shopizer.springboot.order.entity.Order;

/**
 * CBSE Component Implementation
 */
@Service("osgiPaymentProcessor")
public class PaymentProcessorImpl implements PaymentProcessor {

    @Override
    public String process(Order order, BigDecimal amount) {
        // Mock payment processing logic
        System.out.println("[Payment Component] Processing $" + amount + " for Order: " + order.getOrderNumber());
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null; 
        }

        // Generate a mock transaction ID for the report
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * This is the method Maven was complaining about!
     * It identifies the active OSGi-style component.
     */
    @Override
    public String getComponentName() {
        return "Standard-CreditCard-OSGi-Module";
    }
}