package com.shopizer.payment.impl;

import com.shopizer.payment.api.PaymentProcessor;
import com.shopizer.common.entity.Order;
import java.math.BigDecimal;
import java.util.UUID;

public class PaymentProcessorImpl implements PaymentProcessor {

    @Override
    public String process(Order order, BigDecimal amount) {
        System.out.println("[OSGi Payment Component] Processing $" + amount + " for Order: " + order.getOrderNumber());
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null; 
        }

        // Mock success: return a transaction ID
        return "OSGI-TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    @Override
    public String getComponentName() {
        return "Standard-CreditCard-OSGi-Module";
    }
}