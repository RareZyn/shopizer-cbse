package com.shopizer.merchant.dto;

import lombok.Data;

/**
 * DTO for merchant login (FR-015)
 */
@Data
public class LoginRequest {
    private String email;
    private String password;
}
