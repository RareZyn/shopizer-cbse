package com.shopizer.springboot.customer.dto;

/**
 * Customer Profile Update Request DTO
 * FR-026: The system shall allow customers to manage profile (View, Update)
 */
public class CustomerProfileUpdateRequest {

    private String firstName;
    private String lastName;
    private String phone;

    public CustomerProfileUpdateRequest() {}

    public CustomerProfileUpdateRequest(String firstName, String lastName, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    // Getters and Setters
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
