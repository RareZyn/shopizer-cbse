package com.shopizer.merchant.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class InventoryUpdateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stockQuantity;
    private Integer lowStockThreshold;
    private Boolean isActive;
    private Long categoryId;
}
