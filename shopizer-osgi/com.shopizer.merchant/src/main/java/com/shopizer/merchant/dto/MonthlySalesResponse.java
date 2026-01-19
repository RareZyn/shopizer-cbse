package com.shopizer.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MonthlySalesResponse {
    private Integer year;
    private Integer month;
    private Integer orderCount;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Integer itemsSold;
}
