package com.shopizer.customer.dto;

import lombok.Data;

@Data
public class CustomerAuthResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String token;
    private String tokenType = "Bearer";
}
