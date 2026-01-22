package com.shopizer.customer.impl;

import com.shopizer.common.entity.Address;
import com.shopizer.common.entity.Customer;
import com.shopizer.common.entity.Payment;
import com.shopizer.common.exception.BadRequestException;
import com.shopizer.common.exception.ResourceNotFoundException;
import com.shopizer.common.util.JwtTokenProvider;
import com.shopizer.customer.api.CustomerService;
import com.shopizer.customer.dto.*;
import com.shopizer.customer.repository.AddressRepository;
import com.shopizer.customer.repository.CustomerRepository;
import com.shopizer.customer.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                              AddressRepository addressRepository,
                              PaymentRepository paymentRepository,
                              JwtTokenProvider jwtTokenProvider) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.paymentRepository = paymentRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public CustomerResponse register(CustomerRegistrationRequest request) {
        logger.info("Registering new customer: {}", request.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // Validate password match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // Validate password strength
        validatePassword(request.getPassword());

        // Create customer entity
        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setPasswordHash(hashPassword(request.getPassword()));
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setStatus("ACTIVE");
        customer.setEmailVerified(false);

        customer = customerRepository.save(customer);

        logger.info("Customer registered successfully: {}", customer.getEmail());

        return mapToCustomerResponse(customer);
    }

    @Override
    public CustomerLoginResponse login(CustomerLoginRequest request) {
        logger.info("Customer login attempt: {}", request.getEmail());

        Customer customer = customerRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!verifyPassword(request.getPassword(), customer.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        if (!"ACTIVE".equals(customer.getStatus())) {
            throw new BadRequestException("Account is not active. Please contact support.");
        }

        // Update last login time
        customer.setLastLoginAt(java.time.LocalDateTime.now());
        customerRepository.save(customer);

        logger.info("Customer logged in successfully: {}", customer.getEmail());

        // Generate JWT tokens
        String accessToken = jwtTokenProvider.generateToken(customer.getEmail(), customer.getId());
        String refreshToken = jwtTokenProvider.generateToken(customer.getEmail(), customer.getId()); // TODO: Implement proper refresh token

        CustomerLoginResponse response = new CustomerLoginResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(3600L); // 1 hour
        response.setCustomer(mapToCustomerResponse(customer));

        return response;
    }

    @Override
    public void logout(Long customerId) {
        logger.info("Customer logout: {}", customerId);
        // Logout is typically handled client-side by discarding the token
        // In a production system, you might want to blacklist the token here
        logger.info("Customer logged out successfully: {}", customerId);
    }

    @Override
    public CustomerResponse getCustomerById(Long customerId) {
        logger.info("Fetching customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        return mapToCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerByEmail(String email) {
        logger.info("Fetching customer by email: {}", email);
        Customer customer = customerRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        return mapToCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerProfile(Long customerId) {
        logger.info("Fetching customer profile: {}", customerId);
        return getCustomerById(customerId);
    }

    @Override
    public CustomerResponse updateProfile(Long customerId, CustomerProfileUpdateRequest request) {
        logger.info("Updating profile for customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToCustomerResponse(updatedCustomer);
    }

    @Override
    public List<CustomerResponse> getAllCustomers() {
        logger.info("Fetching all customers");
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
            .map(this::mapToCustomerResponse)
            .collect(Collectors.toList());
    }

    @Override
    public CustomerResponse createCustomer(CustomerRegistrationRequest request) {
        logger.info("Admin creating customer: {}", request.getEmail());
        // Same as register but without email verification requirement
        return register(request);
    }

    @Override
    public CustomerResponse updateCustomer(Long customerId, CustomerUpdateRequest request) {
        logger.info("Admin updating customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            customer.setPhone(request.getPhone());
        }
        if (request.getEmail() != null && !request.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already exists: " + request.getEmail());
            }
            customer.setEmail(request.getEmail());
        }
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return mapToCustomerResponse(updatedCustomer);
    }

    @Override
    public void deleteCustomer(Long customerId) {
        logger.info("Admin deleting customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));
        customerRepository.delete(customer);
    }

    @Override
    public AddressResponse createAddress(Long customerId, AddressRequest request) {
        logger.info("Creating address for customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        if (Boolean.TRUE.equals(request.getIsDefault())) {
            List<Address> existingAddresses = addressRepository.findByCustomerId(customerId);
            existingAddresses.forEach(addr -> {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            });
        }

        Address address = new Address();
        address.setCustomer(customer);
        address.setRecipientName(customer.getFirstName() + " " + customer.getLastName());
        address.setAddressLine1(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        Address savedAddress = addressRepository.save(address);
        return mapToAddressResponse(savedAddress);
    }

    @Override
    public AddressResponse getAddressById(Long customerId, Long addressId) {
        logger.info("Fetching address {} for customer: {}", addressId, customerId);
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Address does not belong to this customer");
        }

        return mapToAddressResponse(address);
    }

    @Override
    public List<AddressResponse> getAddresses(Long customerId) {
        logger.info("Fetching addresses for customer: {}", customerId);
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        return addresses.stream()
            .map(this::mapToAddressResponse)
            .collect(Collectors.toList());
    }

    @Override
    public AddressResponse updateAddress(Long customerId, Long addressId, AddressRequest request) {
        logger.info("Updating address {} for customer {}", addressId, customerId);
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Address does not belong to this customer");
        }

        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            List<Address> addresses = addressRepository.findByCustomerId(customerId);
            addresses.forEach(addr -> {
                if (!addr.getId().equals(addressId)) {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                }
            });
        }

        address.setAddressLine1(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        Address updatedAddress = addressRepository.save(address);
        return mapToAddressResponse(updatedAddress);
    }

    @Override
    public void deleteAddress(Long customerId, Long addressId) {
        logger.info("Deleting address {} for customer {}", addressId, customerId);
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Address does not belong to this customer");
        }

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByCustomerId(customerId);
            if (!remainingAddresses.isEmpty()) {
                Address firstAddress = remainingAddresses.get(0);
                firstAddress.setIsDefault(true);
                addressRepository.save(firstAddress);
            }
        }
    }

    @Override
    public AddressResponse addAddress(Long customerId, AddressRequest request) {
        // Alias for createAddress
        return createAddress(customerId, request);
    }

    @Override
    public void setDefaultAddress(Long customerId, Long addressId) {
        logger.info("Setting default address {} for customer {}", addressId, customerId);
        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        if (!address.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Address does not belong to this customer");
        }

        // Unset all other default addresses
        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        addresses.forEach(addr -> {
            addr.setIsDefault(addr.getId().equals(addressId));
            addressRepository.save(addr);
        });
    }

    @Override
    public CustomerResponse validateToken(String token) {
        logger.info("Validating JWT token");
        try {
            if (!jwtTokenProvider.validateToken(token)) {
                throw new BadRequestException("Invalid or expired token");
            }

            Long customerId = jwtTokenProvider.getUserIdFromToken(token);
            return getCustomerById(customerId);
        } catch (Exception e) {
            logger.error("Token validation failed", e);
            throw new BadRequestException("Invalid or expired token");
        }
    }

    @Override
    public void changePassword(Long customerId, PasswordChangeRequest request) {
        logger.info("Changing password for customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        // Verify current password
        if (!verifyPassword(request.getCurrentPassword(), customer.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Validate new password
        validatePassword(request.getNewPassword());

        // Update password
        customer.setPasswordHash(hashPassword(request.getNewPassword()));
        customerRepository.save(customer);
    }

    @Override
    public void deactivateAccount(Long customerId) {
        logger.info("Deactivating account for customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        customer.setStatus("INACTIVE");
        customerRepository.save(customer);
    }

    @Override
    public void activateAccount(Long customerId) {
        logger.info("Activating account for customer: {}", customerId);
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        customer.setStatus("ACTIVE");
        customerRepository.save(customer);
    }

    // Helper methods

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new BadRequestException("Password must be at least 8 characters long");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new BadRequestException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*[a-z].*")) {
            throw new BadRequestException("Password must contain at least one lowercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new BadRequestException("Password must contain at least one digit");
        }
    }

    private String hashPassword(String password) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return org.mindrot.jbcrypt.BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }

    private CustomerResponse mapToCustomerResponse(Customer customer) {
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

    private AddressResponse mapToAddressResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setStreet(address.getAddressLine1());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setCountry(address.getCountry());
        response.setPostalCode(address.getPostalCode());
        response.setIsDefault(address.getIsDefault());
        return response;
    }

    @Override
    public List<PaymentHistoryResponse> getPaymentHistory(Long customerId) {
        logger.info("Fetching payment history for customer: {}", customerId);

        // Verify customer exists
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        List<Payment> payments = paymentRepository.findByCustomerId(customerId);

        return payments.stream()
            .map(this::mapToPaymentHistoryResponse)
            .collect(Collectors.toList());
    }

    private PaymentHistoryResponse mapToPaymentHistoryResponse(Payment payment) {
        PaymentHistoryResponse response = new PaymentHistoryResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder() != null ? payment.getOrder().getId() : null);
        response.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : null);
        response.setGateway(payment.getPaymentMethod() != null ? payment.getPaymentMethod().toString() : null);
        response.setTransactionId(payment.getTransactionId());
        response.setAmount(payment.getAmount());
        response.setCurrency("USD"); // Default currency, could be retrieved from order or payment
        response.setStatus(payment.getStatus() != null ? payment.getStatus().toString() : null);
        response.setCreatedAt(payment.getCreatedAt());
        return response;
    }
}
