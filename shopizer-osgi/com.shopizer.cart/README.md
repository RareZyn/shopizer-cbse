# Cart Module (com.shopizer.cart)

Shopping Cart Management Module for Shopizer OSGI Architecture

## Overview

The Cart module handles all shopping cart operations including adding items, updating quantities, removing items, and cart validation before checkout. It implements functional requirements FR-006 to FR-009.

## Module Information

- **Bundle Symbolic Name:** `com.shopizer.cart`
- **Version:** 1.0.0
- **Dependencies:**
  - `com.shopizer.common` (entities, exceptions)
  - `com.shopizer.catalog` (product validation)

## Exported Packages

### com.shopizer.cart.api
- `CartService` - Main OSGI service interface

### com.shopizer.cart.dto
- `CartItemRequest` - Request DTO for adding items
- `CartItemResponse` - Response DTO for cart items
- `CartResponse` - Complete cart response
- `CartValidationResponse` - Validation result with errors/warnings

## Service Interface (CartService)

### Methods

#### Cart Management Operations
```java
// Create and retrieve carts
Cart createCart(Cart cart)
List<Cart> getAllCarts()
Optional<Cart> getCartById(Long id)
CartResponse getCartByCustomerId(Long customerId)

// Delete operations
void deleteCartById(Long id)
void clearCartById(Long cartId)
```

#### Cart Item Operations (by Cart ID)
```java
// Add, update, remove items using cart ID
CartResponse addItemToCart(Long cartId, CartItemRequest request)
CartResponse updateCartItemById(Long cartId, Long itemId, Integer quantity)
void removeItemFromCartById(Long cartId, Long itemId)
```

#### Cart Item Operations (by Customer ID - Legacy)
```java
// Customer-centric operations
CartResponse addToCart(Long customerId, CartItemRequest request)
CartResponse addItemToCustomerCart(Long customerId, CartItemRequest request)
CartResponse viewCart(Long customerId)
CartResponse updateCartItem(Long customerId, Long itemId, Integer quantity)
void removeFromCart(Long customerId, Long itemId)
void clearCart(Long customerId)
```

#### Cart Calculations
```java
BigDecimal calculateTotal(Long customerId)
BigDecimal getCartTotalById(Long cartId)
Integer getCartItemCount(Long customerId)
```

#### Cart Validation
```java
CartValidationResponse validateCart(Long customerId)
```

#### Cart Merge (for anonymous to authenticated transition)
```java
CartResponse mergeCart(Long anonymousCartId, Long customerId)
```

## Implementation Details

### CartServiceImpl

**Key Features:**
1. **Product Validation:** Uses CatalogService to validate products exist and have sufficient stock
2. **Stock Checking:** Validates stock availability before adding/updating items
3. **Duplicate Prevention:** Merges quantities if same product added multiple times
4. **Cart Validation:** Comprehensive validation checking product availability, stock, and price changes
5. **Anonymous Cart Merge:** Supports merging anonymous cart with customer cart after login

**Business Logic:**
- Automatically creates cart if customer doesn't have one
- Validates stock availability using CatalogService
- Calculates subtotals and totals
- Provides low stock warnings
- Detects price changes since item was added

### Repositories

#### CartRepository
```java
Optional<Cart> findByCustomerId(Long customerId)
void deleteByCustomerId(Long customerId)
```

#### CartItemRepository
```java
List<CartItem> findByCartId(Long cartId)
Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId)
void deleteByCartId(Long cartId)
```

## OSGI Integration

### Bundle Activator (CartActivator)

**Lifecycle Management:**
1. **Start Phase:**
   - Opens ServiceTracker for CatalogService
   - Waits for CatalogService availability
   - Registers CartService when dependency available

2. **Stop Phase:**
   - Closes ServiceTracker
   - Unregisters CartService
   - Cleans up resources

**Service Dependencies:**
- **Required:** CatalogService (for product validation)
- **Optional:** None

## Usage Examples

### Adding Item to Cart
```java
// Get CartService from OSGI registry
ServiceReference<CartService> ref = context.getServiceReference(CartService.class);
CartService cartService = context.getService(ref);

// Create request
CartItemRequest request = new CartItemRequest();
request.setProductId(1L);
request.setQuantity(2);
request.setPrice(new BigDecimal("29.99"));

// Add to cart
CartResponse cart = cartService.addToCart(customerId, request);
```

### Validating Cart Before Checkout
```java
CartValidationResponse validation = cartService.validateCart(customerId);

if (!validation.getIsValid()) {
    // Handle errors
    for (String error : validation.getErrors()) {
        System.out.println("Error: " + error);
    }
}

// Check warnings (e.g., low stock, price changes)
for (String warning : validation.getWarnings()) {
    System.out.println("Warning: " + warning);
}
```

### Merging Anonymous Cart
```java
// After user logs in
CartResponse mergedCart = cartService.mergeCart(anonymousCartId, customerId);
```

## Validation Rules

### Stock Validation
- ✅ Product must exist
- ✅ Product must be active
- ✅ Sufficient stock available
- ⚠️ Low stock warning (< 10 items)

### Cart Validation (Before Checkout)
- ❌ Cart cannot be empty
- ❌ All products must exist
- ❌ All products must be active
- ❌ Sufficient stock for all items
- ⚠️ Price changes detected
- ⚠️ Low stock warnings

## Error Handling

### Exceptions Thrown
- `ResourceNotFoundException` - Cart, item, or product not found
- `InsufficientStockException` - Not enough stock available
- `BadRequestException` - Invalid quantity or unauthorized access

## Data Flow

```
Customer Request
     ↓
CartService API
     ↓
CartServiceImpl (Business Logic)
     ├─> CatalogService (Product Validation)
     ├─> CartRepository (Cart CRUD)
     └─> CartItemRepository (Item CRUD)
```

## Integration with Other Modules

### Dependencies (Imports)
1. **Common Module:**
   - Cart, CartItem entities
   - Exception classes
   - API response DTOs

2. **Catalog Module:**
   - CatalogService (product validation)
   - Product stock checking

### Consumers (Exports to)
1. **Order Module:**
   - Will use CartService to create orders from cart
   - Will validate cart before order creation

2. **Web/REST Layer:**
   - Will expose cart operations via REST endpoints

## Files Created

```
com.shopizer.cart/
├── pom.xml                                      ✅
└── src/main/java/com/shopizer/cart/
    ├── api/
    │   └── CartService.java                     ✅ (20 methods - updated)
    ├── dto/
    │   ├── CartItemRequest.java                 ✅
    │   ├── CartItemResponse.java                ✅
    │   ├── CartResponse.java                    ✅
    │   └── CartValidationResponse.java          ✅
    ├── repository/
    │   ├── CartRepository.java                  ✅
    │   ├── CartRepositoryImpl.java              ✅ (Manual JPA)
    │   ├── CartItemRepository.java              ✅
    │   └── CartItemRepositoryImpl.java          ✅ (Manual JPA)
    ├── impl/
    │   └── CartServiceImpl.java                 ✅ (20 methods implemented)
    └── activator/
        └── CartActivator.java                   ✅ (OSGI lifecycle)

com.shopizer.rest/
└── src/main/java/com/shopizer/rest/servlet/
    └── CartServlet.java                         ✅ (20 REST endpoints)
```

**Total Files:** 12
**Lines of Code:** ~1200
**REST Endpoints:** 20

## Build & Test

### Build Module
```bash
cd com.shopizer.cart
mvn clean install
```

### Expected Output
```
[INFO] Building Shopizer Cart Module 1.0.0
[INFO]
[INFO] --- maven-bundle-plugin:5.1.9:bundle (default-bundle) @ com.shopizer.cart ---
[INFO] Bundle com.shopizer.cart:1.0.0
[INFO] Export-Package: com.shopizer.cart.api, com.shopizer.cart.dto
[INFO] Import-Package: com.shopizer.common.entity, com.shopizer.catalog.api, ...
[INFO] BUILD SUCCESS
```

### Verify Bundle
```bash
unzip -p target/com.shopizer.cart-1.0.0.jar META-INF/MANIFEST.MF
```

## Next Steps

1. **Order Module** will depend on CartService to:
   - Retrieve cart items
   - Validate cart before order creation
   - Clear cart after successful order

2. **REST API Layer** exposes (via CartServlet):

   **Cart Management:**
   - `GET /api/v1/cart` - Get all carts (Admin)
   - `GET /api/v1/cart?customerId={id}` - View cart by customer ID
   - `GET /api/v1/cart/{id}` - Get cart by ID
   - `GET /api/v1/cart/customer/{customerId}` - Get cart by customer ID
   - `POST /api/v1/cart` - Create new cart
   - `DELETE /api/v1/cart/{id}` - Delete cart by ID
   - `DELETE /api/v1/cart?customerId={id}` - Clear cart by customer ID
   - `DELETE /api/v1/cart/{cartId}/clear` - Clear cart items by cart ID

   **Cart Items (by cart ID):**
   - `POST /api/v1/cart/{cartId}/items` - Add item by cart ID
   - `POST /api/v1/cart/customer/{customerId}/items` - Add item by customer ID
   - `PUT /api/v1/cart/{cartId}/items/{itemId}` - Update item by cart ID
   - `DELETE /api/v1/cart/{cartId}/items/{itemId}` - Remove item by cart ID

   **Cart Items (legacy - customer ID in params):**
   - `POST /api/v1/cart/items` - Add item (customerId in body)
   - `PUT /api/v1/cart/items/{itemId}?customerId={id}&quantity={qty}` - Update quantity
   - `DELETE /api/v1/cart/items/{itemId}?customerId={id}` - Remove item

   **Cart Calculations:**
   - `GET /api/v1/cart/total?customerId={id}` - Get total by customer ID
   - `GET /api/v1/cart/{cartId}/total` - Get total by cart ID
   - `GET /api/v1/cart/count?customerId={id}` - Get item count

   **Cart Validation & Merge:**
   - `POST /api/v1/cart/validate?customerId={id}` - Validate cart
   - `POST /api/v1/cart/merge?anonymousCartId={id}&customerId={id}` - Merge carts

## Testing Checklist

- [ ] Add item to cart
- [ ] Update item quantity
- [ ] Remove item from cart
- [ ] Clear cart
- [ ] Calculate total
- [ ] Validate cart with valid items
- [ ] Validate cart with out-of-stock items
- [ ] Validate cart with inactive products
- [ ] Merge anonymous cart
- [ ] Handle duplicate products

---

**Status:** ✅ Complete (Feature Parity with SpringBoot)
**Version:** 1.0.0
**Service Methods:** 20
**REST Endpoints:** 20
**Last Updated:** 2026-01-22
