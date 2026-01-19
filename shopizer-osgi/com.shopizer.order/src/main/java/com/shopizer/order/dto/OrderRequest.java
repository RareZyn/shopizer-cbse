package com.shopizer.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private Long customerId;
    private Long cartId;
    private AddressDTO shippingAddress;
    private AddressDTO billingAddress;
    private String shippingMethod;
    private String paymentMethod;
    private BigDecimal shippingCost;
    private BigDecimal tax;
    private String notes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
        private String street;
        private String city;
        private String state;
        private String country;
        private String postalCode;
    }
}
