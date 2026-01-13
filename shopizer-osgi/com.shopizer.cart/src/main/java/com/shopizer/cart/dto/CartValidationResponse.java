package com.shopizer.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartValidationResponse {
    private Boolean isValid;
    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();

    public void addError(String error) {
        this.errors.add(error);
        this.isValid = false;
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public static CartValidationResponse valid() {
        CartValidationResponse response = new CartValidationResponse();
        response.setIsValid(true);
        return response;
    }
}
