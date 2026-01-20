package com.shopizer.springboot.merchant.exception;

/**
 * Thrown when authentication fails for a merchant (FR-015)
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
