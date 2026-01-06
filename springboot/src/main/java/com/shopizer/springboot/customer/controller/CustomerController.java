package com.shopizer.springboot.customer.controller;

import com.shopizer.springboot.customer.dto.*;
import com.shopizer.springboot.customer.entity.Customer;
import com.shopizer.springboot.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Customer REST Controller
 * FR-023: The system shall allow customers to view payment history
 * FR-024: The system shall allow customers to register new account
 * FR-025: The system shall allow customers to login and logout
 * FR-026: The system shall allow customers to manage profile (View, Update)
 */
@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer", description = "Customer management APIs (FR-023 to FR-026)")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ==================== FR-024: Customer Registration ====================

    @PostMapping("/register")
    @Operation(summary = "Register a new customer account",
            description = "FR-024: The system shall allow customers to register new account. " +
                    "Validates email format, password strength, and checks for duplicate accounts.")
    public ResponseEntity<CustomerResponse> register(@RequestBody CustomerRegistrationRequest request) {
        CustomerResponse response = customerService.registerCustomer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== FR-025: Customer Login/Logout ====================

    @PostMapping("/login")
    @Operation(summary = "Customer login",
            description = "FR-025: The system shall allow customers to login. " +
                    "Returns JWT access token and refresh token upon successful authentication.")
    public ResponseEntity<CustomerLoginResponse> login(@RequestBody CustomerLoginRequest request) {
        CustomerLoginResponse response = customerService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{customerId}/logout")
    @Operation(summary = "Customer logout",
            description = "FR-025: The system shall allow customers to logout. " +
                    "Client should discard tokens after this call.")
    public ResponseEntity<Void> logout(@PathVariable Long customerId) {
        customerService.logout(customerId);
        return ResponseEntity.ok().build();
    }

    // ==================== FR-026: Profile Management ====================

    @GetMapping("/{customerId}/profile")
    @Operation(summary = "Get customer profile",
            description = "FR-026: The system shall allow customers to view their profile. " +
                    "Returns customer details including name, email, phone, and account status.")
    public ResponseEntity<CustomerResponse> getProfile(@PathVariable Long customerId) {
        CustomerResponse response = customerService.getProfile(customerId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{customerId}/profile")
    @Operation(summary = "Update customer profile",
            description = "FR-026: The system shall allow customers to update their profile. " +
                    "Allows updating first name, last name, and phone number.")
    public ResponseEntity<CustomerResponse> updateProfile(
            @PathVariable Long customerId,
            @RequestBody CustomerProfileUpdateRequest request) {
        CustomerResponse response = customerService.updateProfile(customerId, request);
        return ResponseEntity.ok(response);
    }

    // ==================== FR-023: Payment History ====================

    @GetMapping("/{customerId}/payments")
    @Operation(summary = "Get customer payment history",
            description = "FR-023: The system shall allow customers to view payment history. " +
                    "Returns list of all payment transactions for the customer.")
    public ResponseEntity<List<PaymentHistoryResponse>> getPaymentHistory(@PathVariable Long customerId) {
        List<PaymentHistoryResponse> payments = customerService.getPaymentHistory(customerId);
        return ResponseEntity.ok(payments);
    }

    // ==================== Basic CRUD Operations (Admin) ====================

    @PostMapping
    @Operation(summary = "Create a customer (Admin)", description = "Admin operation to create customer directly")
    public ResponseEntity<Customer> createCustomer(@RequestBody Customer customer) {
        return new ResponseEntity<>(customerService.createCustomer(customer), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all customers (Admin)", description = "Admin operation to list all customers")
    public ResponseEntity<List<Customer>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Get customer details by ID")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get customer by email", description = "Find customer by email address")
    public ResponseEntity<Customer> getCustomerByEmail(@PathVariable String email) {
        return customerService.getCustomerByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer (Admin)", description = "Admin operation to update customer")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.updateCustomer(id, customer));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer (Admin)", description = "Admin operation to delete customer account")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
