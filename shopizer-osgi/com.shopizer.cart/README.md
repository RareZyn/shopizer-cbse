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

#### Cart Operations
```java
CartResponse addToCart(Long customerId, CartItemRequest request)
CartResponse viewCart(Long customerId)
CartResponse updateCartItem(Long customerId, Long itemId, Integer quantity)
void removeFromCart(Long customerId, Long itemId)
void clearCart(Long customerId)
```

#### Cart Calculations
```java
BigDecimal calculateTotal(Long customerId)
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
    │   └── CartService.java                     ✅ (9 methods)
    ├── dto/
    │   ├── CartItemRequest.java                 ✅
    │   ├── CartItemResponse.java                ✅
    │   ├── CartResponse.java                    ✅
    │   └── CartValidationResponse.java          ✅
    ├── repository/
    │   ├── CartRepository.java                  ✅
    │   └── CartItemRepository.java              ✅
    ├── impl/
    │   └── CartServiceImpl.java                 ✅ (Full implementation)
    └── activator/
        └── CartActivator.java                   ✅ (OSGI lifecycle)
```

**Total Files:** 9
**Lines of Code:** ~600

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

2. **REST API Layer** will expose:
   - `POST /api/v1/cart/items` - Add to cart
   - `GET /api/v1/cart` - View cart
   - `PUT /api/v1/cart/items/{id}` - Update quantity
   - `DELETE /api/v1/cart/items/{id}` - Remove item
   - `POST /api/v1/cart/validate` - Validate cart

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

**Status:** ✅ Complete
**Version:** 1.0.0
**Last Updated:** 2026-01-13
