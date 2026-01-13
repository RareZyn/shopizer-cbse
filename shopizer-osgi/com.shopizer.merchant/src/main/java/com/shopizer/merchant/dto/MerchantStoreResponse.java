package com.shopizer.merchant.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MerchantStoreResponse {
    private Long id;
    private Long merchantId;
    private String storeName;
    private String description;
    private String storeEmail;
    private String storePhone;
    private String currency;
    private String language;
    private Boolean active;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
