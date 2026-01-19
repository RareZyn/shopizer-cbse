package com.shopizer.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long orderId;
    private String orderNumber;
    private BigDecimal amount;
    private String paymentMethod;
    private String status; // PENDING, AUTHORIZED, CAPTURED, COMPLETED, FAILED
    private String transactionId;
    private String gatewayResponse;
    private LocalDateTime paymentDate;
    private Boolean success;
    private String errorMessage;
}
