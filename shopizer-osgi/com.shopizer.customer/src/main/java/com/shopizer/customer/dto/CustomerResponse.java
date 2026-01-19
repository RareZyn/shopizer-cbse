package com.shopizer.customer.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CustomerResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<AddressResponse> addresses;
}
