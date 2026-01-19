package com.shopizer.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerProfileUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
}
