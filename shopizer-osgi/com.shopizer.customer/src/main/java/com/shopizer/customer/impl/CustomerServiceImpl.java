package com.shopizer.customer.impl;

import com.shopizer.common.entity.Address;
import com.shopizer.common.entity.Customer;
import com.shopizer.common.exception.BadRequestException;
import com.shopizer.common.exception.ResourceNotFoundException;
import com.shopizer.common.util.JwtTokenProvider;
import com.shopizer.customer.api.CustomerService;
import com.shopizer.customer.dto.*;
import com.shopizer.customer.repository.AddressRepository;
import com.shopizer.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class CustomerServiceImpl implements CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                              AddressRepository addressRepository,
                              JwtTokenProvider jwtTokenProvider) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public CustomerAuthResponse register(CustomerRegistrationRequest request) {
        logger.info("Registering new customer: {}", request.getEmail());

        // Check if email already exists
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered: " + request.getEmail());
        }

        // Validate password strength
        validatePassword(request.getPassword());

        // Create customer entity
        Customer customer = new Customer();
        customer.setEmail(request.getEmail());
        customer.setPasswordHash(hashPassword(request.getPassword()));
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setActive(true);

        customer = customerRepository.save(customer);

        logger.info("Customer registered successfully: {}", customer.getEmail());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(customer.getId().toString(), customer.getEmail());

        return mapToAuthResponse(customer, token);
    }

    @Override
    public CustomerAuthResponse login(CustomerLoginRequest request) {
        logger.info("Customer login attempt: {}", request.getEmail());

        // Find customer by email
        Customer customer = customerRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        // Verify password
        if (!verifyPassword(request.getPassword(), customer.getPasswordHash())) {
            throw new BadRequestException("Invalid email or password");
        }

        // Check if account is active
        if (!customer.getActive()) {
            throw new BadRequestException("Account is deactivated. Please contact support.");
        }

        logger.info("Customer logged in successfully: {}", customer.getEmail());

        // Generate JWT token
        String token = jwtTokenProvider.generateToken(customer.getId().toString(), customer.getEmail());

        return mapToAuthResponse(customer, token);
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
    public CustomerResponse updateProfile(Long customerId, CustomerUpdateRequest request) {
        logger.info("Updating profile for customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            customer.setPhoneNumber(request.getPhoneNumber());
        }

        customer = customerRepository.save(customer);

        logger.info("Profile updated successfully");

        return mapToCustomerResponse(customer);
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

        logger.info("Password changed successfully");
    }

    @Override
    public AddressResponse addAddress(Long customerId, AddressRequest request) {
        logger.info("Adding address for customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        // If this is the first address or marked as default, set all others to non-default
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            List<Address> existingAddresses = addressRepository.findByCustomerId(customerId);
            existingAddresses.forEach(addr -> {
                addr.setIsDefault(false);
                addressRepository.save(addr);
            });
        }

        Address address = new Address();
        address.setCustomer(customer);
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        address = addressRepository.save(address);

        logger.info("Address added successfully");

        return mapToAddressResponse(address);
    }

    @Override
    public List<AddressResponse> getAddresses(Long customerId) {
        logger.info("Fetching addresses for customer: {}", customerId);

        // Verify customer exists
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

        // Verify address belongs to customer
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Address does not belong to this customer");
        }

        // If setting as default, unset other default addresses
        if (Boolean.TRUE.equals(request.getIsDefault()) && !address.getIsDefault()) {
            List<Address> addresses = addressRepository.findByCustomerId(customerId);
            addresses.forEach(addr -> {
                if (!addr.getId().equals(addressId)) {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                }
            });
        }

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()));

        address = addressRepository.save(address);

        logger.info("Address updated successfully");

        return mapToAddressResponse(address);
    }

    @Override
    public void deleteAddress(Long customerId, Long addressId) {
        logger.info("Deleting address {} for customer {}", addressId, customerId);

        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Verify address belongs to customer
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Address does not belong to this customer");
        }

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);

        // If deleted address was default, set first remaining address as default
        if (wasDefault) {
            List<Address> remainingAddresses = addressRepository.findByCustomerId(customerId);
            if (!remainingAddresses.isEmpty()) {
                Address firstAddress = remainingAddresses.get(0);
                firstAddress.setIsDefault(true);
                addressRepository.save(firstAddress);
            }
        }

        logger.info("Address deleted successfully");
    }

    @Override
    public void setDefaultAddress(Long customerId, Long addressId) {
        logger.info("Setting default address {} for customer {}", addressId, customerId);

        Address address = addressRepository.findById(addressId)
            .orElseThrow(() -> new ResourceNotFoundException("Address", "id", addressId));

        // Verify address belongs to customer
        if (!address.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Address does not belong to this customer");
        }

        // Unset all other default addresses
        List<Address> addresses = addressRepository.findByCustomerId(customerId);
        addresses.forEach(addr -> {
            addr.setIsDefault(addr.getId().equals(addressId));
            addressRepository.save(addr);
        });

        logger.info("Default address set successfully");
    }

    @Override
    public CustomerResponse validateToken(String token) {
        logger.info("Validating JWT token");

        try {
            if (!jwtTokenProvider.validateToken(token)) {
                throw new BadRequestException("Invalid or expired token");
            }

            String customerId = jwtTokenProvider.getSubject(token);
            return getCustomerById(Long.parseLong(customerId));

        } catch (Exception e) {
            logger.error("Token validation failed", e);
            throw new BadRequestException("Invalid or expired token");
        }
    }

    @Override
    public void deactivateAccount(Long customerId) {
        logger.info("Deactivating account for customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        customer.setActive(false);
        customerRepository.save(customer);

        logger.info("Account deactivated successfully");
    }

    @Override
    public void activateAccount(Long customerId) {
        logger.info("Activating account for customer: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", customerId));

        customer.setActive(true);
        customerRepository.save(customer);

        logger.info("Account activated successfully");
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
        // TODO: Implement proper password hashing (BCrypt)
        // For now, this is a placeholder
        return "hashed_" + password;
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        // TODO: Implement proper password verification (BCrypt)
        // For now, this is a placeholder
        return hashedPassword.equals("hashed_" + plainPassword);
    }

    private CustomerAuthResponse mapToAuthResponse(Customer customer, String token) {
        CustomerAuthResponse response = new CustomerAuthResponse();
        response.setId(customer.getId());
        response.setEmail(customer.getEmail());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setToken(token);
        return response;
    }

    private CustomerResponse mapToCustomerResponse(Customer customer) {
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setEmail(customer.getEmail());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setPhoneNumber(customer.getPhoneNumber());
        response.setActive(customer.getActive());
        response.setCreatedAt(customer.getCreatedAt());
        response.setUpdatedAt(customer.getUpdatedAt());

        // Load addresses
        List<Address> addresses = addressRepository.findByCustomerId(customer.getId());
        response.setAddresses(addresses.stream()
            .map(this::mapToAddressResponse)
            .collect(Collectors.toList()));

        return response;
    }

    private AddressResponse mapToAddressResponse(Address address) {
        AddressResponse response = new AddressResponse();
        response.setId(address.getId());
        response.setStreet(address.getStreet());
        response.setCity(address.getCity());
        response.setState(address.getState());
        response.setCountry(address.getCountry());
        response.setPostalCode(address.getPostalCode());
        response.setIsDefault(address.getIsDefault());
        return response;
    }
}
