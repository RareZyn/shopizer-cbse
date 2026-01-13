package com.shopizer.merchant.dto;

import lombok.Data;

@Data
public class MerchantStoreRequest {
    private Long merchantId;
    private String storeName;
    private String description;
    private String storeEmail;
    private String storePhone;
    private String currency;
    private String language;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}
