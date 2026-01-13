package com.shopizer.customer.dto;

import lombok.Data;

@Data
public class CustomerRegistrationRequest {
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phoneNumber;
}
