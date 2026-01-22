package com.shopizer.springboot.cart.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Cart Validation Response DTO
 * Contains validation results for cart items before checkout
 */
public class CartValidationResponse {

    private boolean valid;
    private List<String> errors;
    private List<String> warnings;

    public CartValidationResponse() {
        this.valid = true;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public static CartValidationResponse valid() {
        return new CartValidationResponse();
    }

    public void addError(String error) {
        this.errors.add(error);
        this.valid = false;
    }

    public void addWarning(String warning) {
        this.warnings.add(warning);
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
        this.valid = errors.isEmpty();
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }
}
