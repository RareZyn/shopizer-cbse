package com.shopizer.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class InventoryItemResponse {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private Boolean isActive;
    private Boolean isLowStock;
    private LocalDateTime createdAt;    
    private LocalDateTime updatedAt;
}
