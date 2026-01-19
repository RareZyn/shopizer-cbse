package com.shopizer.springboot.customer.service;

import com.shopizer.springboot.common.util.JwtTokenProvider;
import com.shopizer.springboot.customer.dto.*;
import com.shopizer.springboot.customer.entity.Customer;
import com.shopizer.springboot.customer.repository.CustomerRepository;
import com.shopizer.springboot.payment.entity.Payment;
import com.shopizer.springboot.payment.repository.PaymentRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Customer Service Implementation
 * FR-023: View payment history
 * FR-024: Register new account
 * FR-025: Login and logout
 * FR-026: Manage profile (View, Update)
 */
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final BCryptPasswordEncoder passwordEncoder;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               PaymentRepository paymentRepository,
                               JwtTokenProvider jwtTokenProvider) {
        this.customerRepository = customerRepository;
        this.paymentRepository = paymentRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ==================== FR-024: Customer Registration ====================

    @Override
    @Transactional
    public CustomerResponse registerCustomer(CustomerRegistrationRequest request) {
        // Validate request
        validateRegistrationRequest(request);

        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email has been used");
        }

        // Create new customer
        Customer customer = new Customer();
        customer.setEmail(request.getEmail().toLowerCase().trim());
        customer.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        customer.setFirstName(request.getFirstName().trim());
        customer.setLastName(request.getLastName().trim());
        customer.setPhone(request.getPhone() != null ? request.getPhone().trim() : null);
        customer.setStatus("ACTIVE");
        customer.setEmailVerified(false);

        Customer savedCustomer = customerRepository.save(customer);
        return mapToResponse(savedCustomer);
    }

    private void validateRegistrationRequest(CustomerRegistrationRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!isValidEmail(request.getEmail())) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (request.getPassword().length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(emailRegex);
    }

    // ==================== FR-025: Customer Login/Logout ====================

    @Override
    @Transactional
    public CustomerLoginResponse login(CustomerLoginRequest request) {
        // Validate request
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        // Find customer by email
        Customer customer = customerRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new IllegalArgumentException("Account not found. Please register your account"));

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), customer.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        // Check if account is active
        if (!"ACTIVE".equals(customer.getStatus())) {
            throw new IllegalArgumentException("Account is not active");
        }

        // Update last login timestamp
        customer.setLastLoginAt(LocalDateTime.now());
        customerRepository.save(customer);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(customer.getId(), customer.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(customer.getId());

        // Build response
        CustomerLoginResponse response = new CustomerLoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(jwtTokenProvider.getExpirationTime());
        response.setCustomer(mapToResponse(customer));

        return response;
    }

    @Override
    @Transactional
    public void logout(Long customerId) {
        // In a stateless JWT system, logout is typically handled client-side
        // by removing the token. For additional security, we can implement
        // token blacklisting or update last activity timestamp.
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Update activity - could be extended to invalidate refresh tokens
        customerRepository.save(customer);
    }

    // ==================== FR-026: Profile Management ====================

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getProfile(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        return mapToResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateProfile(Long customerId, CustomerProfileUpdateRequest request) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        // Update fields if provided
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            customer.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().trim().isEmpty()) {
            customer.setLastName(request.getLastName().trim());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone().trim());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToResponse(updatedCustomer);
    }

    // ==================== FR-023: Payment History ====================

    @Override
    @Transactional(readOnly = true)
    public List<PaymentHistoryResponse> getPaymentHistory(Long customerId) {
        // Verify customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new IllegalArgumentException("Customer not found");
        }

        List<Payment> payments = paymentRepository.findByCustomerId(customerId);

        return payments.stream()
                .map(this::mapToPaymentHistoryResponse)
                .collect(Collectors.toList());
    }

    private PaymentHistoryResponse mapToPaymentHistoryResponse(Payment payment) {
        PaymentHistoryResponse response = new PaymentHistoryResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setGateway(payment.getGateway());
        response.setTransactionId(payment.getTransactionId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setStatus(payment.getStatus());
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }

    // ==================== Basic CRUD Operations ====================

    @Override
    public Customer createCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    @Override
    public Optional<Customer> getCustomerByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    @Override
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    @Override
    @Transactional
    public Customer updateCustomer(Long id, Customer customer) {
        Customer existingCustomer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        existingCustomer.setFirstName(customer.getFirstName());
        existingCustomer.setLastName(customer.getLastName());
        existingCustomer.setEmail(customer.getEmail());
        existingCustomer.setPhone(customer.getPhone());
        return customerRepository.save(existingCustomer);
    }

    @Override
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }

    // ==================== Utility Methods ====================

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Override
    public CustomerResponse mapToResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setEmail(customer.getEmail());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setPhone(customer.getPhone());
        response.setEmailVerified(customer.getEmailVerified());
        response.setStatus(customer.getStatus());
        response.setLastLoginAt(customer.getLastLoginAt());
        response.setCreatedAt(customer.getCreatedAt());
        return response;
    }
}
