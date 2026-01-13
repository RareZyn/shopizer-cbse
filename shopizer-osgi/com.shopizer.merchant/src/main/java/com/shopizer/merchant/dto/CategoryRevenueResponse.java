package com.shopizer.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CategoryRevenueResponse {
    private Long categoryId;
    private String categoryName;
    private BigDecimal revenue;
    private Integer orderCount;
    private Integer itemsSold;
    private BigDecimal percentageOfTotal;
}
