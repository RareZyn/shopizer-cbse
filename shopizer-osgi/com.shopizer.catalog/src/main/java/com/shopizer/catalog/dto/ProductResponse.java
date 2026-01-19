package com.shopizer.catalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String sku;
    private String imageUrl;
    private String categoryName;
    private Long categoryId;
    private String storeName;
    private Long storeId;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
