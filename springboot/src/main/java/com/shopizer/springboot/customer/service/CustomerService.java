package com.shopizer.springboot.customer.service;

import com.shopizer.springboot.customer.dto.*;
import com.shopizer.springboot.customer.entity.Customer;
import java.util.List;
import java.util.Optional;

/**
 * Customer Service Interface
 * FR-023: View payment history
 * FR-024: Register new account
 * FR-025: Login and logout
 * FR-026: Manage profile (View, Update)
 */
public interface CustomerService {

    // FR-024: Customer Registration
    CustomerResponse registerCustomer(CustomerRegistrationRequest request);

    // FR-025: Customer Authentication
    CustomerLoginResponse login(CustomerLoginRequest request);
    void logout(Long customerId);

    // FR-026: Profile Management
    CustomerResponse getProfile(Long customerId);
    CustomerResponse updateProfile(Long customerId, CustomerProfileUpdateRequest request);

    // FR-023: Payment History
    List<PaymentHistoryResponse> getPaymentHistory(Long customerId);

    // Basic CRUD operations
    Customer createCustomer(Customer customer);
    Optional<Customer> getCustomerById(Long id);
    Optional<Customer> getCustomerByEmail(String email);
    List<Customer> getAllCustomers();
    Customer updateCustomer(Long id, Customer customer);
    void deleteCustomer(Long id);

    // Utility methods
    boolean existsByEmail(String email);
    CustomerResponse mapToResponse(Customer customer);

    // Password management
    void changePassword(Long customerId, PasswordChangeRequest request);

    // Address management
    AddressResponse addAddress(Long customerId, AddressRequest request);
    AddressResponse getAddressById(Long customerId, Long addressId);
    List<AddressResponse> getAddresses(Long customerId);
    AddressResponse updateAddress(Long customerId, Long addressId, AddressRequest request);
    void deleteAddress(Long customerId, Long addressId);
    void setDefaultAddress(Long customerId, Long addressId);
}
