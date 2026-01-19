package com.shopizer.customer.dto;

import lombok.Data;

@Data
public class AddressResponse {
    private Long id;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Boolean isDefault;
}
