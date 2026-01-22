package com.shopizer.springboot.common.exception;

import java.util.ArrayList;
import java.util.List;

/**
 * Exception thrown when request data is invalid
 */
public class BadRequestException extends RuntimeException {

    private List<String> errors;

    /**
     * Constructor with message only
     */
    public BadRequestException(String message) {
        super(message);
        this.errors = new ArrayList<>();
    }

    /**
     * Constructor with message and errors
     */
    public BadRequestException(String message, List<String> errors) {
        super(message);
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    /**
     * Constructor with message and cause
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
        this.errors = new ArrayList<>();
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
