package com.shopizer.springboot.payment.service;

/**
 * Payment Gateway Interface
 * FR-020: The system shall integrate with payment gateways (Stripe, PayPal)
 */
public interface PaymentGateway {
    // Gateway methods will be implemented here
    // - processPayment(PaymentRequest request)
    // - refundPayment(String transactionId, BigDecimal amount)
    // - validateCard(String cardNumber, String cvv, String expiryMonth, String expiryYear)
    // - tokenizeCard(CardDetails cardDetails)
    // - getTransactionStatus(String transactionId)
}
