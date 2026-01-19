package com.shopizer.customer.dto;

import lombok.Data;

@Data
public class CustomerLoginRequest {
    private String email;
    private String password;
}
