package com.shopizer.customer.api;

import com.shopizer.customer.dto.*;

import java.util.List;

/**
 * Customer Service Interface
 * Provides customer management, authentication, and profile services
 *
 * Functional Requirements:
 * - FR-024: Customer Registration
 * - FR-025: Customer Login/Authentication
 * - FR-026: Profile Management
 * - FR-027: Address Management
 */
public interface CustomerService {

    /**
     * Register a new customer
     * FR-024: Customer Registration
     *
     * @param request Registration details
     * @return Registered customer with auth token
     */
    CustomerAuthResponse register(CustomerRegistrationRequest request);

    /**
     * Authenticate customer and generate JWT token
     * FR-025: Customer Login
     *
     * @param request Login credentials
     * @return Customer details with JWT token
     */
    CustomerAuthResponse login(CustomerLoginRequest request);

    /**
     * Get customer profile by ID
     * FR-026: Profile Management
     *
     * @param customerId Customer ID
     * @return Customer profile
     */
    CustomerResponse getCustomerById(Long customerId);

    /**
     * Get customer profile by email
     *
     * @param email Customer email
     * @return Customer profile
     */
    CustomerResponse getCustomerByEmail(String email);

    /**
     * Update customer profile
     * FR-026: Profile Management
     *
     * @param customerId Customer ID
     * @param request Update details
     * @return Updated customer profile
     */
    CustomerResponse updateProfile(Long customerId, CustomerUpdateRequest request);

    /**
     * Change customer password
     * FR-026: Profile Management
     *
     * @param customerId Customer ID
     * @param request Password change details
     */
    void changePassword(Long customerId, PasswordChangeRequest request);

    /**
     * Add new address to customer profile
     * FR-027: Address Management
     *
     * @param customerId Customer ID
     * @param request Address details
     * @return Created address
     */
    AddressResponse addAddress(Long customerId, AddressRequest request);

    /**
     * Get all addresses for customer
     * FR-027: Address Management
     *
     * @param customerId Customer ID
     * @return List of addresses
     */
    List<AddressResponse> getAddresses(Long customerId);

    /**
     * Update existing address
     * FR-027: Address Management
     *
     * @param customerId Customer ID
     * @param addressId Address ID
     * @param request Updated address details
     * @return Updated address
     */
    AddressResponse updateAddress(Long customerId, Long addressId, AddressRequest request);

    /**
     * Delete address
     * FR-027: Address Management
     *
     * @param customerId Customer ID
     * @param addressId Address ID
     */
    void deleteAddress(Long customerId, Long addressId);

    /**
     * Set default address for customer
     * FR-027: Address Management
     *
     * @param customerId Customer ID
     * @param addressId Address ID
     */
    void setDefaultAddress(Long customerId, Long addressId);

    /**
     * Validate JWT token and get customer
     *
     * @param token JWT token
     * @return Customer details if token is valid
     */
    CustomerResponse validateToken(String token);

    /**
     * Deactivate customer account
     *
     * @param customerId Customer ID
     */
    void deactivateAccount(Long customerId);

    /**
     * Activate customer account
     *
     * @param customerId Customer ID
     */
    void activateAccount(Long customerId);
}
