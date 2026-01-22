package com.shopizer.springboot.merchant.dto;

/**
 * Authentication response for merchant register/login (FR-015)
 */
public record MerchantAuthResponse(
        Long merchantId,
        String name,
        String email,
        String token
) {}
