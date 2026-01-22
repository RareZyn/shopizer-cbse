package com.shopizer.merchant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for merchant profile update (FR-015)
 * All fields are optional - only provided fields will be updated
 */
@Data
public class MerchantUpdateRequest {
    @JsonProperty("name")
    private String businessName;

    private String phone;
    private String status;
}
