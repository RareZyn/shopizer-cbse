package com.shopizer.springboot.merchant.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
// import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
/**
 * Inventory Update Request DTO
 * FR-016: The system shall allow merchants to manage their product inventory
 */

public record InventoryUpdateRequest(
        @Min(0) Integer stockQuantity,
        @DecimalMin("0.00") BigDecimal price,
        @Size(min = 1, max = 255) String name,
        String description,
        @Min(0) Integer lowStockThreshold,
        Boolean isActive
) {}
