package com.shopizer.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal shippingCost;
    private BigDecimal total;
    private String shippingAddress;
    private String billingAddress;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
