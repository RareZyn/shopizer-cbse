package com.shopizer.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductSalesResponse {
    private Long productId;
    private String productName;
    private Integer quantitySold;
    private BigDecimal totalRevenue;
    private BigDecimal averagePrice;
}
