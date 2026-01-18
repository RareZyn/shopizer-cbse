package com.shopizer.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String status;
}
