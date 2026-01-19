package com.shopizer.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailySalesResponse {
    private LocalDate date;
    private Integer orderCount;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private Integer itemsSold;
}
