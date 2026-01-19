package com.shopizer.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private String status;
    private List<OrderItemResponse> items = new ArrayList<>();
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private String shippingAddress;
    private String billingAddress;
    private PaymentInfo paymentInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentInfo {
        private String paymentMethod;
        private String paymentStatus;
        private String transactionId;
        private LocalDateTime paymentDate;
    }
}
