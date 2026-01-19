package com.shopizer.order.payment;

import com.shopizer.common.entity.Order;
import java.math.BigDecimal;

/**
 * Payment Processor Interface (CBSE Component Contract)
 * Interface for payment gateway implementations (FR-019 to FR-023)
 *
 * This is a key CBSE interface that allows different payment gateway
 * implementations to be plugged in without changing the Order module.
 */
public interface PaymentProcessor {

    /**
     * Process payment for an order
     * @param order Order to process payment for
     * @param amount Amount to charge
     * @return Transaction ID from payment gateway
     */
    String process(Order order, BigDecimal amount);

    /**
     * Get the payment processor name
     * @return Component name (e.g., "Stripe", "PayPal")
     */
    String getComponentName();

    /**
     * Authorize payment (hold funds)
     * @param order Order to authorize
     * @param amount Amount to authorize
     * @return Authorization ID
     */
    String authorize(Order order, BigDecimal amount);

    /**
     * Capture authorized payment
     * @param authorizationId Authorization ID
     * @param amount Amount to capture
     * @return Transaction ID
     */
    String capture(String authorizationId, BigDecimal amount);

    /**
     * Refund payment
     * @param transactionId Original transaction ID
     * @param amount Amount to refund
     * @return Refund transaction ID
     */
    String refund(String transactionId, BigDecimal amount);

    /**
     * Check if payment gateway is available
     * @return true if available, false otherwise
     */
    boolean isAvailable();
}
