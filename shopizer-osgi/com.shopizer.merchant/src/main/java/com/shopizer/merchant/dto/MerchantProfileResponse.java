package com.shopizer.merchant.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for merchant profile (FR-015)
 */
@Data
public class MerchantProfileResponse {
    private Long id;
    private String businessName;
    private String email;
    private String phone;
    private Boolean active;
    private LocalDateTime createdAt;
}
