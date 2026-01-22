# Merchant Module - FR-015 Implementation Summary

## ✅ Completed Implementation

### FR-015: Merchant Registration and Login

#### API Methods
```java
MerchantProfileResponse registerMerchant(MerchantRegistrationRequest request);
AuthResponse login(LoginRequest request);
```

#### DTOs Created
1. **MerchantRegistrationRequest**
   - businessName
   - email
   - password
   - phone

2. **LoginRequest**
   - email
   - password

3. **AuthResponse**
   - accessToken
   - tokenType
   - expiresAt
   - merchant (MerchantProfileResponse)

4. **MerchantProfileResponse**
   - id
   - businessName
   - email
   - phone
   - active
   - createdAt

#### Repository Created
- **MerchantRepository** extends JpaRepository<Merchant, Long>
  - `findByEmail(String email)`
  - `findByBusinessRegistrationNumber(String brn)`

#### Implementation Features

**Registration (`registerMerchant`)**:
- Validates all required fields
- Checks email uniqueness
- Validates business registration number uniqueness
- Password validation (minimum 8 characters)
- SHA-256 password hashing
- Auto-activates new merchant accounts
- Returns merchant profile (without password)

**Login (`login`)**:
- Authenticates via email/password
- Verifies account is active
- Generates JWT token (24-hour expiry)
- Returns AuthResponse with token + merchant profile

**Security**:
- Password hashing using SHA-256 (upgrade to BCrypt recommended)
- JWT token generation via `JwtTokenProvider`
- Token includes: email (subject), userId (claim)
- 24-hour token TTL
- Secure password verification

**Validation**:
- Business name required
- Email required and unique
- Password minimum 8 characters
- Business registration number required and unique
- Account active status check on login
- Dependency availability checks

#### Activator Updates
- Added `MerchantRepository` initialization
- Added `JwtTokenProvider` initialization with module-specific secret
- Updated constructor to pass all 5 dependencies
- JWT secret: 64-character module-specific key

#### POM Updates
- Added import for `com.shopizer.common.util` (JwtTokenProvider)
- Exports `com.shopizer.merchant.api` and `com.shopizer.merchant.dto`

## Testing Scenarios

### Happy Path
```java
// 1. Register new merchant
MerchantRegistrationRequest regReq = new MerchantRegistrationRequest();
regReq.setBusinessName("Tech Store LLC");
regReq.setEmail("owner@techstore.com");
regReq.setPassword("SecurePass123");
regReq.setBusinessRegistrationNumber("BRN-2025-001");
regReq.setPhone("+1-555-0100");

MerchantProfileResponse profile = merchantService.registerMerchant(regReq);
// Returns: id, businessName, email, phone, active=true, createdAt

// 2. Login
LoginRequest loginReq = new LoginRequest();
loginReq.setEmail("owner@techstore.com");
loginReq.setPassword("SecurePass123");

AuthResponse auth = merchantService.login(loginReq);
// Returns: accessToken (JWT), tokenType="Bearer", expiresAt, merchant profile
```

### Error Cases
- Duplicate email → BadRequestException("Email is already registered")
- Duplicate BRN → Database constraint violation
- Short password → BadRequestException("Password must be at least 8 characters")
- Invalid credentials → BadRequestException("Invalid credentials")
- Inactive account → BadRequestException("Merchant account is inactive")

## File Structure
```
com.shopizer.merchant/
├── src/main/java/com/shopizer/merchant/
│   ├── api/
│   │   └── MerchantService.java (updated with 2 new methods)
│   ├── impl/
│   │   └── MerchantServiceImpl.java (added registration/login + helpers)
│   ├── dto/
│   │   ├── MerchantRegistrationRequest.java (NEW)
│   │   ├── LoginRequest.java (NEW)
│   │   ├── AuthResponse.java (NEW)
│   │   └── MerchantProfileResponse.java (NEW)
│   ├── repository/
│   │   ├── MerchantRepository.java (NEW)
│   │   └── MerchantStoreRepository.java (existing)
│   └── activator/
│       └── MerchantActivator.java (updated with JWT provider)
├── pom.xml (updated imports)
└── README.md (existing)
```

## Next Steps for Complete System

### REST API Layer
Create `com.shopizer.rest` bundle with JAX-RS endpoints:

```java
@Path("/api/merchants")
public class MerchantAuthResource {
    
    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response register(MerchantRegistrationRequest request) {
        MerchantProfileResponse profile = merchantService.registerMerchant(request);
        return Response.status(201).entity(profile).build();
    }
    
    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        AuthResponse auth = merchantService.login(request);
        return Response.ok(auth).build();
    }
}
```

### JWT Filter for Protected Routes
```java
@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {
    
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (!jwtTokenProvider.validateToken(token)) {
                requestContext.abortWith(
                    Response.status(401).entity("Invalid token").build()
                );
            }
        }
    }
}
```

### Example API Calls
```bash
# Register merchant
curl -X POST http://localhost:8080/api/merchants/register \
  -H "Content-Type: application/json" \
  -d '{
    "businessName": "Tech Store",
    "email": "owner@techstore.com",
    "password": "SecurePass123",
    "businessRegistrationNumber": "BRN-2025-001",
    "phone": "+1-555-0100"
  }'

# Login
curl -X POST http://localhost:8080/api/merchants/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "owner@techstore.com",
    "password": "SecurePass123"
  }'

# Use token in subsequent requests
curl -X GET http://localhost:8080/api/stores/1 \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

## Build & Deploy
```bash
cd shopizer-osgi/com.shopizer.merchant
mvn clean install

# Deploy order:
# 1. com.shopizer.common
# 2. com.shopizer.catalog
# 3. com.shopizer.order
# 4. com.shopizer.merchant (will auto-register when dependencies available)
```

## Status: ✅ FR-015 COMPLETE
All merchant registration and login functionality implemented with:
- ✅ DTOs (4 classes)
- ✅ Repository (1 interface)
- ✅ Service implementation (2 methods + 5 helpers)
- ✅ Activator updates (JWT provider integration)
- ✅ Security (hashing, validation, JWT)
- ✅ Error handling (comprehensive validation)
