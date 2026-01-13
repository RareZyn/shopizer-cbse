package com.shopizer.springboot.merchant.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Sales Report Response DTO
 * FR-017: The system shall provide merchants with sales reports and analytics
 */

public record SalesReportResponse(
        Long storeId,

        // reportPeriod (startDate, endDate)
        LocalDate startDate,
        LocalDate endDate,

        Long totalOrders,
        BigDecimal totalRevenue,
        BigDecimal averageOrderValue,

        List<TopSellingProduct> topSellingProducts,

        Map<String, BigDecimal> salesByCategory, // categoryName -> sales
        Map<String, BigDecimal> salesByDay,      // "YYYY-MM-DD" -> sales

        Double conversionRate // keep null for now (no views/visits table)
) {
    public record TopSellingProduct(
            Long productId,
            String productName,
            String productSku,
            Long unitsSold,
            BigDecimal totalSales
    ) {}
}
