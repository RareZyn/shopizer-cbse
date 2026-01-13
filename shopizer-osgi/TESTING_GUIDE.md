# Testing Guide - REST API Examples

Quick reference for testing all endpoints with cURL.

## How to Test Each Module

### 1. Catalog API

```bash
# Create category
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics","description":"Electronic devices"}'

# Get all categories
curl http://localhost:8080/api/v1/categories

# Create product
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","description":"Gaming laptop","price":999.99,"stockQuantity":50,"sku":"LAP-001","categoryId":1}'

# Get all products
curl http://localhost:8080/api/v1/products

# Get product by ID
curl http://localhost:8080/api/v1/products/1

# Search products
curl "http://localhost:8080/api/v1/products/search?q=laptop"

# Update product
curl -X PUT http://localhost:8080/api/v1/products/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Gaming Laptop Pro","price":1299.99,"stockQuantity":45,"sku":"LAP-001","categoryId":1}'

# Delete product
curl -X DELETE http://localhost:8080/api/v1/products/1
```

### 2. Cart API

```bash
# Add item to cart
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"productId":1,"quantity":2}'

# View cart
curl "http://localhost:8080/api/v1/cart?customerId=1"

# Update cart item
curl -X PUT "http://localhost:8080/api/v1/cart/items/1?customerId=1&quantity=3"

# Get cart total
curl "http://localhost:8080/api/v1/cart/total?customerId=1"

# Get cart item count
curl "http://localhost:8080/api/v1/cart/count?customerId=1"

# Remove item from cart
curl -X DELETE "http://localhost:8080/api/v1/cart/items/1?customerId=1"

# Clear cart
curl -X DELETE "http://localhost:8080/api/v1/cart?customerId=1"
```

### 3. Customer API

```bash
# Register customer
curl -X POST http://localhost:8080/api/v1/customers/register \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Pass123!","firstName":"John","lastName":"Doe","phoneNumber":"+1234567890"}'

# Login customer
curl -X POST http://localhost:8080/api/v1/customers/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"Pass123!"}'

# Get customer profile
curl http://localhost:8080/api/v1/customers/1

# Update profile
curl -X PUT http://localhost:8080/api/v1/customers/1 \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jonathan","lastName":"Doe","phoneNumber":"+1987654321"}'

# Add address
curl -X POST http://localhost:8080/api/v1/customers/1/addresses \
  -H "Content-Type: application/json" \
  -d '{"streetAddress":"123 Main St","city":"New York","state":"NY","postalCode":"10001","country":"USA","isDefault":true}'

# Get addresses
curl http://localhost:8080/api/v1/customers/1/addresses
```

### 4. Order API

```bash
# Create order
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"shippingAddressId":1,"billingAddressId":1,"paymentMethod":"CREDIT_CARD"}'

# Get order by ID
curl http://localhost:8080/api/v1/orders/1

# Get order history
curl "http://localhost:8080/api/v1/orders/history?customerId=1"

# Process payment
curl -X POST http://localhost:8080/api/v1/orders/1/payment \
  -H "Content-Type: application/json" \
  -d '{"paymentMethod":"STRIPE","amount":1999.98,"cardNumber":"4111111111111111","cvv":"123","expiryDate":"12/2027"}'

# Update order status
curl -X PUT http://localhost:8080/api/v1/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status":"PROCESSING"}'

# Track order
curl http://localhost:8080/api/v1/orders/1/tracking

# Cancel order
curl -X POST http://localhost:8080/api/v1/orders/1/cancel \
  -H "Content-Type: application/json" \
  -d '{"reason":"Customer requested cancellation"}'
```

### 5. Merchant API

```bash
# Create store
curl -X POST http://localhost:8080/api/v1/merchants/stores \
  -H "Content-Type: application/json" \
  -d '{"merchantId":1,"storeName":"Tech Store","description":"Electronics store","email":"store@tech.com","phoneNumber":"+1234567890","address":"789 Store Ave"}'

# Get store
curl http://localhost:8080/api/v1/merchants/stores/1

# Get inventory
curl http://localhost:8080/api/v1/merchants/1/inventory

# Update stock
curl -X PUT http://localhost:8080/api/v1/merchants/1/inventory/1 \
  -H "Content-Type: application/json" \
  -d '{"quantity":100}'

# Add stock
curl -X POST http://localhost:8080/api/v1/merchants/1/inventory/1/add \
  -H "Content-Type: application/json" \
  -d '{"quantity":50}'

# Get low stock items
curl "http://localhost:8080/api/v1/merchants/1/inventory/low-stock?threshold=20"

# Get sales report
curl "http://localhost:8080/api/v1/merchants/1/reports/sales?start=2026-01-01&end=2026-01-31"

# Get revenue analytics
curl "http://localhost:8080/api/v1/merchants/1/analytics/revenue?start=2026-01-01&end=2026-01-31"

# Get total revenue
curl http://localhost:8080/api/v1/merchants/1/analytics/total-revenue
```

## Complete E-Commerce Flow

```bash
# 1. Create category
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Content-Type: application/json" \
  -d '{"name":"Electronics"}'

# 2. Create product
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"MacBook Pro","price":1999.99,"stockQuantity":10,"categoryId":1,"sku":"MBP-001"}'

# 3. Register customer
curl -X POST http://localhost:8080/api/v1/customers/register \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@example.com","password":"Secret123!","firstName":"Alice","lastName":"Smith"}'

# 4. Add address
curl -X POST http://localhost:8080/api/v1/customers/1/addresses \
  -H "Content-Type: application/json" \
  -d '{"streetAddress":"123 Main St","city":"NYC","state":"NY","postalCode":"10001","country":"USA","isDefault":true}'

# 5. Add to cart
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"productId":1,"quantity":1}'

# 6. View cart
curl "http://localhost:8080/api/v1/cart?customerId=1"

# 7. Create order
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"shippingAddressId":1,"billingAddressId":1,"paymentMethod":"STRIPE"}'

# 8. Process payment
curl -X POST http://localhost:8080/api/v1/orders/1/payment \
  -H "Content-Type: application/json" \
  -d '{"paymentMethod":"STRIPE","amount":1999.99,"cardNumber":"4111111111111111","cvv":"123","expiryDate":"12/2027"}'

# 9. Track order
curl http://localhost:8080/api/v1/orders/1/tracking
```

## Using Postman

1. Open Postman
2. Create new request
3. Set method (GET, POST, PUT, DELETE)
4. Enter URL: `http://localhost:8080/api/v1/...`
5. For POST/PUT: Set Body → raw → JSON
6. Paste JSON data
7. Click Send

## Using PowerShell (Windows)

```powershell
# Create product
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/products" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"name":"Laptop","price":999.99,"stockQuantity":50,"sku":"LAP-001"}'

# Get products
Invoke-RestMethod -Uri "http://localhost:8080/api/v1/products"
```

## Quick Response Codes

- **200 OK** - Success
- **201 Created** - Resource created
- **204 No Content** - Deleted successfully
- **400 Bad Request** - Invalid data
- **404 Not Found** - Resource not found
- **500 Internal Server Error** - Server error

---

**For full endpoint documentation:** See `com.shopizer.rest/README.md`
