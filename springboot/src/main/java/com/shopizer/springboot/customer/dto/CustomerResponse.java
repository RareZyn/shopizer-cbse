package com.shopizer.springboot.customer.dto;

import java.time.LocalDateTime;

/**
 * Customer Response DTO
 * FR-024: The system shall allow customers to register new account
 * FR-026: The system shall allow customers to manage profile (View, Update)
 */
public class CustomerResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Boolean emailVerified;
    private String status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;

    public CustomerResponse() {}

    public CustomerResponse(Long id, String email, String firstName, String lastName,
                            String phone, Boolean emailVerified, String status,
                            LocalDateTime lastLoginAt, LocalDateTime createdAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.emailVerified = emailVerified;
        this.status = status;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Boolean getEmailVerified() { return emailVerified; }
    public void setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
