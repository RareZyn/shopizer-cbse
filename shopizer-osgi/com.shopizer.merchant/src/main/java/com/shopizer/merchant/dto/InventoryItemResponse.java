package com.shopizer.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryItemResponse {
    private Long productId;
    private String productName;
    private String sku;
    private Integer stockQuantity;
    private BigDecimal price;
    private String categoryName;
    private Boolean active;
    private LocalDateTime lastUpdated;
}
