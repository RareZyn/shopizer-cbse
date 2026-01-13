package com.shopizer.springboot.merchant.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MerchantProductCreateRequest(
        @NotBlank String sku,
        @NotBlank String name,
        String description,

        @NotNull @DecimalMin("0.00") BigDecimal price,

        @Min(0) Integer stockQuantity,
        @Min(0) Integer lowStockThreshold,

        Boolean isActive,
        Long categoryId
) {}
