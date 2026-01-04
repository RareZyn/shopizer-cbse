package com.shopizer.springboot.payment.service;

/**
 * PayPal Payment Gateway Implementation
 * FR-020: The system shall integrate with payment gateways (Stripe, PayPal)
 */
public class PayPalPaymentGateway implements PaymentGateway {
    // PayPal-specific implementation will be here
    // - Uses PayPal REST API for payment processing
    // - Handles PayPal webhooks/IPN
    // - Manages PayPal order and capture flows
}
