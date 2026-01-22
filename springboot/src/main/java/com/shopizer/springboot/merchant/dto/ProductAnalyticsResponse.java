package com.shopizer.springboot.merchant.dto;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Detailed analytics for a single product (FR-018)
 */
public record ProductAnalyticsResponse(
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
        Map<String, BigDecimal> salesByDay,
        Long viewCount,
        Double conversionRate
) {}
