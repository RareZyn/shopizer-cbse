package com.shopizer.merchant.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for merchant registration (FR-015)
 * Payload: name, email, password, phone.
 */
@Data
public class MerchantRegistrationRequest {
    @JsonProperty("name")
    private String businessName;

    private String email;
    private String password;
    private String phone;
}
