# Customer Module (com.shopizer.customer)

## Overview
The Customer module handles customer registration, authentication, profile management, and address management in the Shopizer e-commerce platform. It implements functional requirements FR-024 to FR-027.

## Architecture

### OSGI Bundle Configuration
- **Bundle-SymbolicName**: com.shopizer.customer
- **Bundle-Activator**: CustomerActivator
- **Version**: 1.0.0

### Dependencies
- **com.shopizer.common** - Shared entities and utilities (JwtTokenProvider)

### Service Exports
- `com.shopizer.customer.api.CustomerService` - Customer management interface

## Components

### 1. CustomerService Interface
Main service interface for customer operations:
- `register(CustomerRegistrationRequest)` - Register new customer (FR-024)
- `login(CustomerLoginRequest)` - Authenticate customer (FR-025)
- `getCustomerById(Long)` - Get customer profile (FR-026)
- `getCustomerByEmail(String)` - Get customer by email
- `updateProfile(Long, CustomerUpdateRequest)` - Update profile (FR-026)
- `changePassword(Long, PasswordChangeRequest)` - Change password (FR-026)
- `addAddress(Long, AddressRequest)` - Add new address (FR-027)
- `getAddresses(Long)` - Get all addresses (FR-027)
- `updateAddress(Long, Long, AddressRequest)` - Update address (FR-027)
- `deleteAddress(Long, Long)` - Delete address (FR-027)
- `setDefaultAddress(Long, Long)` - Set default address (FR-027)
- `validateToken(String)` - Validate JWT token
- `deactivateAccount(Long)` - Deactivate customer account
- `activateAccount(Long)` - Activate customer account

### 2. CustomerServiceImpl
Core implementation with:

**Registration Workflow (FR-024)**:
1. Validate email is unique
2. Validate password strength (min 8 chars, uppercase, lowercase, digit)
3. Hash password using BCrypt (TODO)
4. Create customer entity with active status
5. Generate JWT token
6. Return customer with token

**Login Workflow (FR-025)**:
1. Find customer by email
2. Verify password against hash
3. Check if account is active
4. Generate JWT token
5. Return customer with token

**Profile Management (FR-026)**:
- Update first name, last name, phone number
- Change password with current password verification
- Password strength validation
- Account activation/deactivation

**Address Management (FR-027)**:
- Add multiple addresses per customer
- Set default address (only one default per customer)
- Update and delete addresses
- Auto-set first address as default if it's deleted

### 3. CustomerActivator
OSGI lifecycle management:
- Initializes JwtTokenProvider with secret key
- Registers CustomerService in OSGI registry
- No external service dependencies
- Implements graceful shutdown

## Data Transfer Objects (DTOs)

### Request DTOs
- **CustomerRegistrationRequest** - Registration details
  - email, password, firstName, lastName, phoneNumber

- **CustomerLoginRequest** - Login credentials
  - email, password

- **CustomerUpdateRequest** - Profile update
  - firstName, lastName, phoneNumber

- **PasswordChangeRequest** - Password change
  - currentPassword, newPassword

- **AddressRequest** - Address details
  - street, city, state, country, postalCode, isDefault

### Response DTOs
- **CustomerAuthResponse** - Authentication result with JWT
  - id, email, firstName, lastName, phoneNumber, token, tokenType

- **CustomerResponse** - Customer profile
  - id, email, firstName, lastName, phoneNumber, active, timestamps, addresses

- **AddressResponse** - Address details
  - id, street, city, state, country, postalCode, isDefault

## Repositories

### CustomerRepository
- `findByEmail(String)` - Find customer by email
- `existsByEmail(String)` - Check if email exists
- `findById(Long)` - Find customer by ID
- `save(Customer)` - Save customer

### AddressRepository
- `findByCustomerId(Long)` - Get all addresses for customer
- `findById(Long)` - Find address by ID
- `save(Address)` - Save address
- `delete(Address)` - Delete address

## Security Features

### Password Validation
Passwords must meet the following requirements:
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one digit

### JWT Authentication
- Token generated on registration and login
- Token contains customer ID and email
- Token expiration: 24 hours (configurable)
- Token validation for protected operations

### Password Hashing
- Uses BCrypt algorithm (TODO: implement)
- Salt automatically generated
- One-way hashing (cannot reverse)

## Registration and Login Flow

### Registration (FR-024)
```java
CustomerRegistrationRequest request = new CustomerRegistrationRequest();
request.setEmail("john@example.com");
request.setPassword("SecurePass123");
request.setFirstName("John");
request.setLastName("Doe");
request.setPhoneNumber("+1234567890");

CustomerAuthResponse response = customerService.register(request);
String token = response.getToken(); // Use for subsequent requests
```

### Login (FR-025)
```java
CustomerLoginRequest request = new CustomerLoginRequest();
request.setEmail("john@example.com");
request.setPassword("SecurePass123");

CustomerAuthResponse response = customerService.login(request);
String token = response.getToken();
```

### Token Validation
```java
CustomerResponse customer = customerService.validateToken(token);
// Use customer.getId() for operations
```

## Profile Management Flow (FR-026)

### Update Profile
```java
CustomerUpdateRequest request = new CustomerUpdateRequest();
request.setFirstName("John");
request.setLastName("Smith");
request.setPhoneNumber("+1234567890");

CustomerResponse updated = customerService.updateProfile(customerId, request);
```

### Change Password
```java
PasswordChangeRequest request = new PasswordChangeRequest();
request.setCurrentPassword("OldPass123");
request.setNewPassword("NewPass456");

customerService.changePassword(customerId, request);
```

## Address Management Flow (FR-027)

### Add Address
```java
AddressRequest request = new AddressRequest();
request.setStreet("123 Main St");
request.setCity("Springfield");
request.setState("IL");
request.setCountry("USA");
request.setPostalCode("62701");
request.setIsDefault(true);

AddressResponse address = customerService.addAddress(customerId, request);
```

### Get All Addresses
```java
List<AddressResponse> addresses = customerService.getAddresses(customerId);
```

### Set Default Address
```java
customerService.setDefaultAddress(customerId, addressId);
// All other addresses automatically set to non-default
```

## CBSE Principles Applied

### Independent Component
- CustomerService has no dependencies on other business modules
- Self-contained authentication and profile management
- Can be deployed and tested independently

### Service-Oriented Architecture
- Exposed via OSGI service registry
- Interface-based contract (CustomerService)
- Easy integration with other modules

### Separation of Concerns
- Authentication logic in CustomerServiceImpl
- Password hashing isolated in helper methods
- JWT token management delegated to JwtTokenProvider
- Repository layer for data access
- DTO layer for API contracts

## Error Handling

### Exceptions Thrown
- **BadRequestException**
  - Email already registered
  - Invalid email or password
  - Account deactivated
  - Password validation failures
  - Invalid token
  - Address doesn't belong to customer

- **ResourceNotFoundException**
  - Customer not found
  - Address not found

## Integration Points

### JWT Token Usage
Other modules can:
1. Receive JWT token from client
2. Call `validateToken(token)` to get customer details
3. Use customer ID for operations

Example integration:
```java
// In Cart/Order/other modules
String token = request.getHeader("Authorization").substring(7); // Remove "Bearer "
CustomerResponse customer = customerService.validateToken(token);
Long customerId = customer.getId();

// Use customerId for operations
cartService.viewCart(customerId);
```

## Configuration

### JWT Secret Key
- Default: "your-256-bit-secret-key-change-this-in-production"
- TODO: Load from OSGI configuration service
- Should be changed in production
- Minimum 256 bits for HS256 algorithm

### Token Expiration
- Default: 24 hours (86400000 ms)
- Configurable via JwtTokenProvider constructor
- TODO: Load from OSGI configuration service

## Future Enhancements
- Real BCrypt password hashing implementation
- Email verification during registration
- Password reset functionality
- Two-factor authentication (2FA)
- Social login (Google, Facebook)
- Customer preferences and settings
- Profile picture upload
- Account deletion
- Email notification service integration
- Audit log for security events
- Rate limiting for login attempts
- Session management

## Functional Requirements Coverage

| FR ID   | Requirement | Implementation |
|---------|-------------|----------------|
| FR-024  | Customer registration | `register()` with validation |
| FR-025  | Customer login | `login()` with JWT token |
| FR-026  | Profile management | `updateProfile()`, `changePassword()` |
| FR-027  | Address management | `addAddress()`, `updateAddress()`, `deleteAddress()` |

## Testing Notes

### Unit Testing
- Mock CustomerRepository and AddressRepository
- Test password validation rules
- Test email uniqueness validation
- Test JWT token generation and validation
- Test default address management
- Test account activation/deactivation

### Integration Testing
- Test registration to login flow
- Test profile update with database
- Test address CRUD operations
- Test default address switching logic

### OSGI Testing
- Test bundle activation
- Test service registration
- Test service availability
- Test graceful shutdown

## Security Considerations

### Password Security
- Never log passwords
- Hash passwords before storing
- Use strong hashing algorithm (BCrypt with cost factor 12+)
- Validate password strength on registration and change

### Token Security
- Use HTTPS in production
- Store tokens securely on client side
- Short token expiration (24 hours)
- Implement token refresh mechanism
- Invalidate tokens on logout

### Data Privacy
- Never expose password hashes in API responses
- Verify customer ownership before operations
- Implement proper authorization checks
- Log security events (failed logins, password changes)
