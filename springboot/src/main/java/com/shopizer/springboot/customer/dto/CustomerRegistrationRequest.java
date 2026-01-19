package com.shopizer.springboot.customer.dto;

/**
 * Customer Registration Request DTO
 * FR-024: The system shall allow customers to register new account
 */
public class CustomerRegistrationRequest {

    private String email;
    private String password;
    private String confirmPassword;
    private String firstName;
    private String lastName;
    private String phone;

    public CustomerRegistrationRequest() {}

    public CustomerRegistrationRequest(String email, String password, String confirmPassword,
                                       String firstName, String lastName, String phone) {
        this.email = email;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
