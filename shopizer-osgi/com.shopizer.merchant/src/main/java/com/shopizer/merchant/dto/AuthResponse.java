package com.shopizer.merchant.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for merchant authentication response (FR-015)
 */
@Data
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private LocalDateTime expiresAt;
    private MerchantProfileResponse merchant;
}
