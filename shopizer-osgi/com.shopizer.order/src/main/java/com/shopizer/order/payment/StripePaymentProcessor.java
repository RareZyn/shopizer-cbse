package com.shopizer.order.payment;

import com.shopizer.common.entity.Order;
import com.shopizer.common.exception.PaymentProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Stripe Payment Gateway Implementation
 * This is a mock implementation for demonstration
 */
public class StripePaymentProcessor implements PaymentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(StripePaymentProcessor.class);

    @Override
    public String process(Order order, BigDecimal amount) {
        logger.info("Processing Stripe payment for order: {}, amount: {}", order.getOrderNumber(), amount);

        try {
            // Mock payment processing
            // In production, this would call Stripe API
            String transactionId = "stripe_" + UUID.randomUUID().toString();

            logger.info("Stripe payment processed successfully. Transaction ID: {}", transactionId);
            return transactionId;

        } catch (Exception e) {
            logger.error("Stripe payment failed for order: {}", order.getOrderNumber(), e);
            throw new PaymentProcessingException("Stripe payment processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    public String getComponentName() {
        return "Stripe";
    }

    @Override
    public String authorize(Order order, BigDecimal amount) {
        logger.info("Authorizing Stripe payment for order: {}, amount: {}", order.getOrderNumber(), amount);
        // Mock authorization
        return "stripe_auth_" + UUID.randomUUID().toString();
    }

    @Override
    public String capture(String authorizationId, BigDecimal amount) {
        logger.info("Capturing Stripe payment. Authorization ID: {}, amount: {}", authorizationId, amount);
        // Mock capture
        return "stripe_capture_" + UUID.randomUUID().toString();
    }

    @Override
    public String refund(String transactionId, BigDecimal amount) {
        logger.info("Processing Stripe refund. Transaction ID: {}, amount: {}", transactionId, amount);
        // Mock refund
        return "stripe_refund_" + UUID.randomUUID().toString();
    }

    @Override
    public boolean isAvailable() {
        // In production, check if Stripe API keys are configured
        return true;
    }
}
