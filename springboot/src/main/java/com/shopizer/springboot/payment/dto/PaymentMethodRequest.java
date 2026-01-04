package com.shopizer.springboot.payment.dto;

/**
 * Payment Method Request DTO
 * FR-019: The system shall support multiple payment methods
 * FR-023: The system shall validate payment information securely
 */
public class PaymentMethodRequest {
    // Request fields will be implemented here
    // - type (CREDIT_CARD, DEBIT_CARD, PAYPAL)
    // - cardNumber (to be tokenized)
    // - expiryMonth
    // - expiryYear
    // - cvv (to be validated, not stored)
    // - cardholderName
    // - setAsDefault
}
