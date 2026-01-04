# Shopizer CBSE - Spring Boot Backend

## Project Overview
Component-Based Software Engineering (CBSE) e-commerce backend inspired by Shopizer, built with Spring Boot 4.0.1 and Java 21.

## Technology Stack
- **Framework:** Spring Boot 4.0.1
- **Java Version:** 21
- **Database:** Supabase (PostgreSQL)
- **API Documentation:** Swagger/OpenAPI 3.0
- **Authentication:** JWT

## Package Structure
```
com.shopizer.springboot/
├── catalog/          # Product & Category Management (FR-001 to FR-005)
├── cart/             # Shopping Cart Management (FR-006 to FR-009)
├── order/            # Order Processing (FR-010 to FR-014)
├── merchant/         # Merchant & Store Management (FR-015 to FR-018)
├── payment/          # Payment Processing (FR-019 to FR-023)
├── customer/         # Customer Management (FR-024 to FR-027)
├── config/           # Application Configuration
└── common/           # Shared Components
```

## Module Architecture
Each business module follows a layered architecture:
```
module/
├── entity/           # JPA Entities (Data Layer)
├── dto/              # Request/Response DTOs (Presentation Layer)
├── repository/       # Data Access Interfaces (Data Layer)
├── service/          # Business Logic Interfaces (Business Layer)
│   └── *Impl.java    # Service Implementations
└── controller/       # REST Controllers (Presentation Layer)
```

---

## Functional Requirements by Module

### Catalog Module (FR-001 to FR-005)
| FR | Description |
|----|-------------|
| FR-001 | Product CRUD operations |
| FR-002 | Category CRUD operations |
| FR-003 | Search products by keyword |
| FR-004 | Browse products by category |
| FR-005 | Track product stock levels |

**Files:**
- `entity/Product.java` - Product entity
- `entity/Category.java` - Category entity
- `dto/ProductRequest.java` - Product creation/update request
- `dto/ProductResponse.java` - Product response
- `dto/CategoryRequest.java` - Category creation/update request
- `dto/CategoryResponse.java` - Category response
- `dto/ProductSearchRequest.java` - Search parameters
- `repository/ProductRepository.java` - Product data access
- `repository/CategoryRepository.java` - Category data access
- `service/CatalogService.java` - Service interface
- `service/CatalogServiceImpl.java` - Service implementation
- `controller/CatalogController.java` - REST endpoints

**API Endpoints:**
```
POST   /api/v1/catalog/products              - Create product
GET    /api/v1/catalog/products              - Get all products
GET    /api/v1/catalog/products/{id}         - Get product by ID
PUT    /api/v1/catalog/products/{id}         - Update product
DELETE /api/v1/catalog/products/{id}         - Delete product
POST   /api/v1/catalog/categories            - Create category
GET    /api/v1/catalog/categories            - Get all categories
GET    /api/v1/catalog/categories/{id}       - Get category by ID
PUT    /api/v1/catalog/categories/{id}       - Update category
DELETE /api/v1/catalog/categories/{id}       - Delete category
GET    /api/v1/catalog/products/search       - Search products
GET    /api/v1/catalog/categories/{id}/products - Browse by category
GET    /api/v1/catalog/products/{id}/stock   - Get stock level
```

---

### Cart Module (FR-006 to FR-009)
| FR | Description |
|----|-------------|
| FR-006 | Cart item management (Add, View, Update, Remove) |
| FR-007 | Calculate and display cart total |
| FR-008 | Validate product availability before adding |
| FR-009 | Persist cart data for logged-in customers |

**Files:**
- `entity/Cart.java` - Cart entity
- `entity/CartItem.java` - Cart item entity
- `dto/CartRequest.java` - Cart request
- `dto/CartResponse.java` - Cart response with total
- `dto/CartItemRequest.java` - Cart item request
- `dto/CartItemResponse.java` - Cart item response
- `repository/CartRepository.java` - Cart data access
- `repository/CartItemRepository.java` - Cart item data access
- `service/CartService.java` - Service interface
- `service/CartServiceImpl.java` - Service implementation
- `controller/CartController.java` - REST endpoints

**API Endpoints:**
```
GET    /api/v1/cart                    - Get cart for current customer
POST   /api/v1/cart/items              - Add item to cart
PUT    /api/v1/cart/items/{itemId}     - Update cart item quantity
DELETE /api/v1/cart/items/{itemId}     - Remove item from cart
DELETE /api/v1/cart                    - Clear cart
GET    /api/v1/cart/total              - Get cart total
POST   /api/v1/cart/merge              - Merge guest cart with logged-in cart
```

---

### Order Module (FR-010 to FR-014)
| FR | Description |
|----|-------------|
| FR-010 | Place orders from cart |
| FR-011 | Calculate order totals (taxes, shipping) |
| FR-012 | Track order status |
| FR-013 | View order history |
| FR-014 | Send order confirmation notifications |

**Files:**
- `entity/Order.java` - Order entity
- `entity/OrderItem.java` - Order item entity
- `dto/OrderRequest.java` - Order placement request
- `dto/OrderResponse.java` - Order response
- `dto/OrderItemResponse.java` - Order item response
- `dto/OrderStatusUpdateRequest.java` - Status update request
- `repository/OrderRepository.java` - Order data access
- `repository/OrderItemRepository.java` - Order item data access
- `service/OrderService.java` - Service interface
- `service/OrderServiceImpl.java` - Service implementation
- `controller/OrderController.java` - REST endpoints

**Order Status Values:**
- `PENDING` - Order placed, awaiting processing
- `PROCESSING` - Order being prepared
- `SHIPPED` - Order dispatched
- `DELIVERED` - Order delivered
- `CANCELLED` - Order cancelled

**API Endpoints:**
```
POST   /api/v1/orders                        - Place order from cart
GET    /api/v1/orders                        - Get order history
GET    /api/v1/orders/{id}                   - Get order by ID
GET    /api/v1/orders/number/{orderNumber}   - Get order by order number
PUT    /api/v1/orders/{id}/status            - Update order status
POST   /api/v1/orders/{id}/cancel            - Cancel order
GET    /api/v1/orders/{id}/totals            - Get order totals breakdown
POST   /api/v1/orders/{id}/resend-confirmation - Resend confirmation
```

---

### Merchant Module (FR-015 to FR-018)
| FR | Description |
|----|-------------|
| FR-015 | Manage store profile |
| FR-016 | Manage product inventory |
| FR-017 | Sales reports and analytics |
| FR-018 | Configure shipping options |

**Files:**
- `entity/Merchant.java` - Merchant entity
- `entity/MerchantStore.java` - Store entity
- `entity/ShippingOption.java` - Shipping option entity
- `dto/MerchantRequest.java` - Merchant request
- `dto/MerchantResponse.java` - Merchant response
- `dto/MerchantStoreRequest.java` - Store request
- `dto/MerchantStoreResponse.java` - Store response
- `dto/ShippingOptionRequest.java` - Shipping option request
- `dto/ShippingOptionResponse.java` - Shipping option response
- `dto/SalesReportResponse.java` - Sales report response
- `dto/InventoryUpdateRequest.java` - Inventory update request
- `repository/MerchantRepository.java` - Merchant data access
- `repository/MerchantStoreRepository.java` - Store data access
- `repository/ShippingOptionRepository.java` - Shipping option data access
- `service/MerchantService.java` - Service interface
- `service/MerchantServiceImpl.java` - Service implementation
- `controller/MerchantController.java` - REST endpoints

**API Endpoints:**
```
# Merchant Management
POST   /api/v1/merchants                             - Create merchant
GET    /api/v1/merchants/{id}                        - Get merchant by ID
PUT    /api/v1/merchants/{id}                        - Update merchant

# Store Management
POST   /api/v1/merchants/{merchantId}/stores         - Create store
GET    /api/v1/merchants/{merchantId}/stores         - Get stores by merchant
GET    /api/v1/stores/{storeId}                      - Get store by ID
PUT    /api/v1/stores/{storeId}                      - Update store

# Inventory Management
PUT    /api/v1/stores/{storeId}/inventory            - Update inventory
GET    /api/v1/stores/{storeId}/inventory            - Get inventory
GET    /api/v1/stores/{storeId}/inventory/low-stock  - Get low stock products

# Sales Reports
GET    /api/v1/stores/{storeId}/reports/sales        - Get sales report
GET    /api/v1/stores/{storeId}/reports/top-products - Get top selling products
GET    /api/v1/stores/{storeId}/reports/by-category  - Get sales by category

# Shipping Options
POST   /api/v1/stores/{storeId}/shipping-options     - Create shipping option
GET    /api/v1/stores/{storeId}/shipping-options     - Get shipping options
PUT    /api/v1/shipping-options/{optionId}           - Update shipping option
DELETE /api/v1/shipping-options/{optionId}           - Delete shipping option
```

---

### Payment Module (FR-019 to FR-023)
| FR | Description |
|----|-------------|
| FR-019 | Support multiple payment methods |
| FR-020 | Integrate with payment gateways (Stripe, PayPal) |
| FR-021 | Process refunds |
| FR-022 | Store payment history |
| FR-023 | Validate payment information securely |

**Files:**
- `entity/Payment.java` - Payment entity
- `entity/PaymentMethod.java` - Payment method entity
- `entity/Refund.java` - Refund entity
- `dto/PaymentRequest.java` - Payment request
- `dto/PaymentResponse.java` - Payment response
- `dto/PaymentMethodRequest.java` - Payment method request
- `dto/PaymentMethodResponse.java` - Payment method response
- `dto/RefundRequest.java` - Refund request
- `dto/RefundResponse.java` - Refund response
- `repository/PaymentRepository.java` - Payment data access
- `repository/PaymentMethodRepository.java` - Payment method data access
- `repository/RefundRepository.java` - Refund data access
- `service/PaymentService.java` - Service interface
- `service/PaymentServiceImpl.java` - Service implementation
- `service/PaymentGateway.java` - Gateway interface
- `service/StripePaymentGateway.java` - Stripe implementation
- `service/PayPalPaymentGateway.java` - PayPal implementation
- `controller/PaymentController.java` - REST endpoints

**Payment Methods:**
- `CREDIT_CARD`
- `DEBIT_CARD`
- `PAYPAL`
- `STRIPE`
- `BANK_TRANSFER`

**Payment Status:**
- `PENDING`
- `COMPLETED`
- `FAILED`
- `REFUNDED`

**API Endpoints:**
```
# Payment Processing
POST   /api/v1/payments                          - Process payment
GET    /api/v1/payments/{id}                     - Get payment by ID
GET    /api/v1/payments/order/{orderId}          - Get payment by order ID

# Payment Methods
POST   /api/v1/payment-methods                   - Add payment method
GET    /api/v1/payment-methods                   - Get customer's payment methods
PUT    /api/v1/payment-methods/{id}/default      - Set as default
DELETE /api/v1/payment-methods/{id}              - Delete payment method
POST   /api/v1/payment-methods/validate          - Validate payment info

# Refunds
POST   /api/v1/refunds                           - Process refund
GET    /api/v1/refunds/{id}                      - Get refund by ID
GET    /api/v1/refunds/payment/{paymentId}       - Get refunds by payment
GET    /api/v1/refunds/order/{orderId}           - Get refunds by order

# Payment History
GET    /api/v1/payments/history                  - Get payment history

# Webhooks
POST   /api/v1/payments/webhooks/stripe          - Stripe webhook
POST   /api/v1/payments/webhooks/paypal          - PayPal webhook
```

---

### Customer Module (FR-024 to FR-027)
| FR | Description |
|----|-------------|
| FR-024 | Customer registration and account management |
| FR-025 | Customer authentication (login/logout) |
| FR-026 | Manage customer addresses |
| FR-027 | View and update customer profile |

**Files:**
- `entity/Customer.java` - Customer entity
- `entity/Address.java` - Address entity
- `dto/CustomerRegistrationRequest.java` - Registration request
- `dto/CustomerLoginRequest.java` - Login request
- `dto/CustomerLoginResponse.java` - Login response (with tokens)
- `dto/CustomerResponse.java` - Customer response
- `dto/CustomerProfileUpdateRequest.java` - Profile update request
- `dto/PasswordChangeRequest.java` - Password change request
- `dto/AddressRequest.java` - Address request
- `dto/AddressResponse.java` - Address response
- `repository/CustomerRepository.java` - Customer data access
- `repository/AddressRepository.java` - Address data access
- `service/CustomerService.java` - Service interface
- `service/CustomerServiceImpl.java` - Service implementation
- `controller/CustomerController.java` - REST endpoints

**Customer Status:**
- `ACTIVE`
- `INACTIVE`
- `SUSPENDED`

**Address Types:**
- `SHIPPING`
- `BILLING`
- `BOTH`

**API Endpoints:**
```
# Registration & Account
POST   /api/v1/customers/register              - Register new customer
DELETE /api/v1/customers/account               - Delete account
PUT    /api/v1/customers/password              - Change password

# Authentication
POST   /api/v1/auth/login                      - Login
POST   /api/v1/auth/logout                     - Logout
POST   /api/v1/auth/refresh                    - Refresh token
POST   /api/v1/auth/verify-email               - Verify email
POST   /api/v1/auth/forgot-password            - Request password reset
POST   /api/v1/auth/reset-password             - Reset password

# Address Management
POST   /api/v1/customers/addresses             - Add address
GET    /api/v1/customers/addresses             - Get all addresses
GET    /api/v1/customers/addresses/{id}        - Get address by ID
PUT    /api/v1/customers/addresses/{id}        - Update address
DELETE /api/v1/customers/addresses/{id}        - Delete address
PUT    /api/v1/customers/addresses/{id}/default - Set as default

# Profile
GET    /api/v1/customers/profile               - Get profile
PUT    /api/v1/customers/profile               - Update profile
```

---

## Configuration Module

**Files:**
- `config/SwaggerConfig.java` - Swagger/OpenAPI configuration
- `config/OpenApiConfig.java` - OpenAPI 3.0 enhanced configuration
- `config/SecurityConfig.java` - Spring Security & JWT configuration
- `config/DatabaseConfig.java` - Supabase/PostgreSQL configuration
- `config/WebConfig.java` - Web MVC configuration

---

## Common Module

### Exceptions
- `ResourceNotFoundException` - 404 errors
- `BadRequestException` - 400 errors
- `UnauthorizedException` - 401 errors
- `ForbiddenException` - 403 errors
- `PaymentProcessingException` - Payment failures
- `InsufficientStockException` - Stock validation failures
- `GlobalExceptionHandler` - Centralized exception handling

### DTOs
- `ApiResponse` - Standard success response wrapper
- `ApiErrorResponse` - Standard error response wrapper
- `PagedResponse` - Paginated response wrapper

### Utilities
- `JwtTokenProvider` - JWT token generation/validation
- `EncryptionUtil` - Data encryption/decryption

---

## API Response Format

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Error Response
```json
{
  "success": false,
  "error": "RESOURCE_NOT_FOUND",
  "message": "Product not found with id: 123",
  "details": [],
  "path": "/api/v1/catalog/products/123",
  "timestamp": "2024-01-15T10:30:00",
  "status": 404
}
```

### Paginated Response
```json
{
  "content": [ ... ],
  "page": 0,
  "size": 20,
  "totalElements": 100,
  "totalPages": 5,
  "first": true,
  "last": false
}
```

---

## Database (Supabase)
- **Provider:** Supabase (PostgreSQL)
- **Configuration:** See `config/DatabaseConfig.java`
- **Connection:** Via JDBC with connection pooling

---

## Development Notes

### Adding a New Feature
1. Create entity in `module/entity/`
2. Create DTOs in `module/dto/`
3. Create repository interface in `module/repository/`
4. Create service interface and implementation in `module/service/`
5. Create REST controller in `module/controller/`
6. Add Swagger documentation annotations
7. Write unit and integration tests

### Coding Standards
- Follow Java naming conventions
- Use constructor injection for dependencies
- Document all public APIs with Javadoc
- Map functional requirements (FR-XXX) to implementations
- Use DTOs for API request/response (never expose entities directly)
