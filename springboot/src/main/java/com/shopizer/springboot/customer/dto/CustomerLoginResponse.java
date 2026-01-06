package com.shopizer.springboot.customer.dto;

/**
 * Customer Login Response DTO
 * FR-025: The system shall allow customers to login and logout
 */
public class CustomerLoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn;
    private CustomerResponse customer;

    public CustomerLoginResponse() {}

    public CustomerLoginResponse(String accessToken, String refreshToken, Long expiresIn, CustomerResponse customer) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.customer = customer;
    }

    // Getters and Setters
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }

    public CustomerResponse getCustomer() { return customer; }
    public void setCustomer(CustomerResponse customer) { this.customer = customer; }
}
