package com.shopizer.springboot.customer.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment History Response DTO
 * FR-023: The system shall allow customers to view payment history
 */
public class PaymentHistoryResponse {

    private Long id;
    private Long orderId;
    private String paymentMethod;
    private String gateway;
    private String transactionId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private LocalDateTime createdAt;

    public PaymentHistoryResponse() {}

    public PaymentHistoryResponse(Long id, Long orderId, String paymentMethod, String gateway,
                                  String transactionId, BigDecimal amount, String currency,
                                  String status, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.gateway = gateway;
        this.transactionId = transactionId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
