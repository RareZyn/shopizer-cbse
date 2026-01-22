package com.shopizer.springboot.merchant.dto;

import java.math.BigDecimal;

/**
 * Aggregate per-product sales report entry (FR-018)
 */
public record ProductReportResponse(
        Long productId,
        String productName,
        String productSku,
        Long storeId,
        String categoryName,
        Integer stockQuantity,
        Integer lowStockThreshold,
        Long ordersCount,
        Long unitsSold,
        BigDecimal totalRevenue,
        Long viewCount,
        Double conversionRate
) {}
