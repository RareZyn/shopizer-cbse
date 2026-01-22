package com.shopizer.merchant.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MerchantProductCreateRequest {
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private String category;
    private Integer stockQuantity;
    private Boolean isActive;
    private Integer lowStockThreshold;
    private Long categoryId;
}
