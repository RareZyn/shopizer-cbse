package com.shopizer.payment.api;

import com.shopizer.common.entity.Order;
import java.math.BigDecimal;

public interface PaymentProcessor {
    String process(Order order, BigDecimal amount);
    String getComponentName(); // Essential for CBSE identifying which bundle is active
}