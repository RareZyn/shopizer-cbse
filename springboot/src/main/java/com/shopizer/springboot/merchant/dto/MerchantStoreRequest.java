package com.shopizer.springboot.merchant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
/**
 * Merchant Store Request DTO
 * FR-015: The system shall allow merchants to manage their store profile
 */
public record MerchantStoreRequest(
        @NotBlank @Size(max = 255) String storeName,
        @NotBlank @Size(max = 50) String storeCode,
        @NotBlank String address, 
        @Size(max = 2000) String description,
        @Size(max = 500) String logoUrl,
        @Size(max = 3) String currency,
        @Size(max = 50) String defaultLanguage,
        Boolean isActive
){}
