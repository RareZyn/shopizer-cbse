package com.shopizer.merchant.dto;

import lombok.Data;

@Data
public class MerchantStoreRequest {    
     private Long merchantId;  
    private String storeName;
    private String storeCode;
    private String logoUrl;
    private String description;
    private String storePhone;
    private String currency;
    private String defaultLanguage;
    private Boolean isActive;
    private String email;
    private String address;

}
