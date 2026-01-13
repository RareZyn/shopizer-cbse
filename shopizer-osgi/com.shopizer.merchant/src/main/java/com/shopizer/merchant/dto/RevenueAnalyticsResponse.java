package com.shopizer.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RevenueAnalyticsResponse {
    private Long storeId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue;
    private BigDecimal averageDailyRevenue;
    private BigDecimal growthRate;
    private List<CategoryRevenueResponse> revenueByCategory;
}
