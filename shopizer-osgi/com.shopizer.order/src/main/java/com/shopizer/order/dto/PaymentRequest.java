package com.shopizer.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String paymentMethod; // CREDIT_CARD, PAYPAL, STRIPE, etc.
    private BigDecimal amount;
    private String cardNumber;
    private String cardHolderName;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String paypalEmail;
    private String paymentToken; // For tokenized payments
}
