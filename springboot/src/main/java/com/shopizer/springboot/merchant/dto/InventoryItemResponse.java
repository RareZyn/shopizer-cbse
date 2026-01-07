package com.shopizer.springboot.merchant.dto;

import java.math.BigDecimal;

public record InventoryItemResponse(
        Long productId,
        String sku,
        String name,
        BigDecimal price,
        Integer stockQuantity,
        Long storeId
) {}
