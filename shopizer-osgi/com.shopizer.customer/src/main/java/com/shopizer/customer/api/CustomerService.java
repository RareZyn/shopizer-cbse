package com.shopizer.customer.api;

import com.shopizer.customer.dto.*;

import java.util.List;

/**
 * OSGI Service Interface for Customer Module
 * Provides customer authentication, profile management, and address management operations
 * 
 * Functional Requirements:
 * - FR-024: Register new account
 * - FR-025: Login and logout
 * - FR-026: Manage profile (View, Update)
 * - FR-027: Manage addresses (CRUD)
 */
public interface CustomerService {

    // FR-024: Register new account
    CustomerResponse register(CustomerRegistrationRequest request);

    // FR-025: Login and logout
    CustomerLoginResponse login(CustomerLoginRequest request);
    void logout(Long customerId);

    // FR-026: Manage profile (View, Update)
    CustomerResponse getCustomerById(Long customerId);
    CustomerResponse getCustomerProfile(Long customerId);
    CustomerResponse getCustomerByEmail(String email);
    CustomerResponse updateProfile(Long customerId, CustomerProfileUpdateRequest request);

    // Admin operations
    List<CustomerResponse> getAllCustomers();
    CustomerResponse createCustomer(CustomerRegistrationRequest request);
    CustomerResponse updateCustomer(Long customerId, CustomerUpdateRequest request);
    void deleteCustomer(Long customerId);

    // FR-027: Manage addresses (CRUD)
    AddressResponse createAddress(Long customerId, AddressRequest request);
    AddressResponse addAddress(Long customerId, AddressRequest request); // Alias for createAddress
    AddressResponse getAddressById(Long customerId, Long addressId);
    List<AddressResponse> getAddresses(Long customerId);
    AddressResponse updateAddress(Long customerId, Long addressId, AddressRequest request);
    void deleteAddress(Long customerId, Long addressId);
    void setDefaultAddress(Long customerId, Long addressId);

    // Additional utility methods
    CustomerResponse validateToken(String token);
    void changePassword(Long customerId, PasswordChangeRequest request);
    void deactivateAccount(Long customerId);
    void activateAccount(Long customerId);

    // FR-023: Payment History
    List<PaymentHistoryResponse> getPaymentHistory(Long customerId);
}
