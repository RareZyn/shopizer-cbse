package com.shopizer.springboot.merchant.dto;

import java.time.Instant;
/**
 * Merchant Store Response DTO
 * FR-015: The system shall allow merchants to manage their store profile
 */
public record MerchantStoreResponse (
    Long id,
    Long merchantId,
    String storeName,
    String storeCode,
    String description,
    String logoUrl,
    String address,
    String currency,
    String defaultLanguage,
    Boolean isActive,
    Instant createdAt,
    Instant updatedAt
){}
