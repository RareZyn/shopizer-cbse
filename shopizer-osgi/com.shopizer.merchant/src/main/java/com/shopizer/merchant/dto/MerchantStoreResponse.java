package com.shopizer.merchant.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantStoreResponse {
    private Long id;
    private Long merchantId;
    private String storeName;
    private String storeCode;
    private String description;
    private String logoUrl;
    private String storeEmail;
    private String storePhone;
    private String currency;
    private String language;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
