# Shopizer OSGI - Component-Based E-Commerce Platform

A modular e-commerce platform built with OSGI architecture, demonstrating component-based software engineering principles.

---

## 🚀 Quick Start

### Build the Project
```bash
cd shopizer-osgi
mvn clean install
```

### Run the Platform
```bash
cd com.shopizer.launcher
mvn exec:java
```

### Test the REST APIs

**Option 1: Using Swagger UI** (Interactive - Easiest!)
```
Open browser: http://localhost:8080/api/docs
```

**Option 2: Using cURL** (Command Line)
```bash
# Create a product
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":999.99,"stockQuantity":50,"sku":"LAP-001"}'

# View all products
curl http://localhost:8080/api/v1/products
```

---

## 📦 Project Structure

```
shopizer-osgi/
├── com.shopizer.common/      # Shared entities, utilities, exceptions
├── com.shopizer.catalog/     # Product & category management
├── com.shopizer.cart/        # Shopping cart operations
├── com.shopizer.order/       # Order processing & payments
├── com.shopizer.customer/    # Customer authentication & profiles
├── com.shopizer.merchant/    # Store management & analytics
├── com.shopizer.rest/        # HTTP REST API endpoints (NEW!)
└── com.shopizer.launcher/    # OSGI runtime launcher
```

---

## 🎯 What Can You Do?

### 1. **Test via REST APIs** (Beginner-Friendly!)

All OSGI services are now accessible via HTTP REST endpoints:

| Module | Endpoints | What You Can Test |
|--------|-----------|-------------------|
| **Catalog** | `/api/v1/products`, `/api/v1/categories` | Create products, manage categories, search |
| **Cart** | `/api/v1/cart` | Add to cart, update quantities, validate checkout |
| **Order** | `/api/v1/orders` | Create orders, process payments, track shipments |
| **Customer** | `/api/v1/customers` | Register, login, manage profile & addresses |
| **Merchant** | `/api/v1/merchants` | Manage stores, inventory, view sales analytics |

**Base URL:** `http://localhost:8080/api/v1`

### 2. **Monitor OSGI Platform**

Interactive console commands:
```bash
shopizer> status      # View bundle states
shopizer> services    # List registered services
shopizer> help        # Show available commands
shopizer> exit        # Shutdown platform
```

---

## 🔧 Module Overview

### Common Module
**Purpose:** Shared entities, utilities, and exceptions
**Exports:** JPA entities (Product, Category, Cart, Order, Customer), utilities (JwtTokenProvider)

### Catalog Module (16 endpoints)
**Service:** `CatalogService`
**Operations:** Product CRUD, category management, search, stock management

### Cart Module (9 endpoints)
**Service:** `CartService`
**Operations:** Add to cart, view cart, update items, validate, merge carts
**Dependencies:** CatalogService (for product validation)

### Order Module (8 endpoints)
**Services:** `OrderService`, `PaymentProcessor`
**Operations:** Create orders, process payments (Stripe/PayPal), track orders, order history
**Dependencies:** CartService, CatalogService

### Customer Module (14 endpoints)
**Service:** `CustomerService`
**Operations:** Registration, login (JWT), profile management, address management
**Dependencies:** JwtTokenProvider

### Merchant Module (20 endpoints)
**Service:** `MerchantService`
**Operations:** Store management, inventory tracking, sales reports, revenue analytics
**Dependencies:** CatalogService

### REST Module (NEW!)
**Purpose:** HTTP REST API layer on top of OSGI services
**Technology:** Jetty 11, Jackson JSON, Jakarta Servlet
**Port:** 8080
**Total Endpoints:** 67 REST APIs

### Launcher Module
**Purpose:** OSGI runtime initialization
**Framework:** Apache Felix 7.0.5
**Features:** Auto-bundle installation, dependency resolution, interactive console

---

## 📖 REST API Examples

### Catalog - Create Product
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "Gaming laptop",
    "price": 999.99,
    "stockQuantity": 50,
    "sku": "LAP-001",
    "categoryId": 1
  }'
```

### Cart - Add to Cart
```bash
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "productId": 1,
    "quantity": 2
  }'
```

### Customer - Register
```bash
curl -X POST http://localhost:8080/api/v1/customers/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "Pass123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Order - Create Order
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "shippingAddressId": 1,
    "billingAddressId": 1,
    "paymentMethod": "CREDIT_CARD"
  }'
```

### Merchant - View Inventory
```bash
curl http://localhost:8080/api/v1/merchants/1/inventory
```

### Merchant - Sales Report
```bash
curl "http://localhost:8080/api/v1/merchants/1/reports/sales?start=2026-01-01&end=2026-01-31"
```

**See all 67 endpoints:** Check `com.shopizer.rest/README.md`

---

## 🏗️ Architecture

### Component Dependencies
```
Common (Base)
  ↓
Catalog
  ↓
Cart ← depends on Catalog
  ↓
Order ← depends on Cart & Catalog

Customer ← depends on Common

Merchant ← depends on Catalog

REST ← depends on all services (HTTP layer)
```

### Service Communication
```
HTTP Client → REST Servlet → OSGI Service Registry → Business Service
```

**Example:**
```
POST /api/v1/products
  ↓
CatalogServlet.doPost()
  ↓
CatalogService.createProduct()
  ↓
Database
```

---

## ⚙️ Configuration

### Prerequisites
- Java 21
- Maven 3.8+
- PostgreSQL (required for data persistence)

### Database Setup

Database configuration is centralized in the **common module**:

**Location:** `com.shopizer.common/src/main/resources/database.properties`

⚠️ **IMPORTANT:** This file is in `.gitignore` and contains sensitive credentials. Never commit it to version control.

```properties
# Update these values for your database
javax.persistence.jdbc.url=jdbc:postgresql://your-host:5432/your-database
javax.persistence.jdbc.user=your_username
javax.persistence.jdbc.password=your_password
```

For detailed database configuration instructions, see [DATABASE_CONFIGURATION_GUIDE.md](DATABASE_CONFIGURATION_GUIDE.md).

### Change REST API Port
Edit `com.shopizer.rest/src/main/java/.../RestActivator.java`:
```java
private static final int HTTP_PORT = 8080;  // Change this
```

---

## 🧪 Testing Workflow

Complete end-to-end test scenario:

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

# 4. Add to cart
curl -X POST http://localhost:8080/api/v1/cart/items \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"productId":1,"quantity":1}'

# 5. View cart
curl "http://localhost:8080/api/v1/cart?customerId=1"

# 6. Create order
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":1,"shippingAddressId":1,"billingAddressId":1,"paymentMethod":"STRIPE"}'
```

---

## 🛠️ Troubleshooting

### Port 8080 Already in Use
**Solution:** Change port in `RestActivator.java` or stop other application using port 8080

### Bundle Not Starting
```bash
shopizer> status     # Check bundle state
shopizer> services   # Verify service registration
```

### Empty API Responses `[]`
**Normal behavior** - Database is empty. Create data using POST requests first.

### Connection Refused
Ensure server is running: Look for "Jetty server started on port 8080" in console

---

## 📊 Project Statistics

- **Modules:** 8 (7 application + 1 launcher)
- **OSGI Services:** 6 business services
- **REST Endpoints:** 67 HTTP APIs
- **Servlets:** 5 REST controllers
- **JPA Entities:** 12 entities
- **Lines of Code:** ~10,000 LOC
- **Build Time:** ~1.5 minutes
- **Startup Time:** ~3 seconds

---

## 🎓 Key Technologies

- **OSGI Framework:** Apache Felix 7.0.5
- **Web Server:** Eclipse Jetty 11.0.18
- **Persistence:** JPA 3.1 + Hibernate 6.4
- **JSON Processing:** Jackson 2.16.0
- **Security:** JWT (JJWT 0.12.3)
- **Logging:** SLF4J 2.0.9
- **Build Tool:** Maven 3.x
- **Java Version:** 21

---

## 📚 Documentation

- **[DATABASE_CONFIGURATION_GUIDE.md](DATABASE_CONFIGURATION_GUIDE.md)** - Database setup and configuration
- **[SWAGGER_GUIDE.md](SWAGGER_GUIDE.md)** - Interactive API documentation
- `TESTING_GUIDE.md` - cURL examples for all endpoints
- `com.shopizer.rest/README.md` - REST API module details
- `com.shopizer.cart/README.md` - Cart module documentation
- `com.shopizer.order/README.md` - Order module documentation
- `com.shopizer.launcher/README.md` - Launcher console guide

---

## ✅ Implementation Status

**All Modules Completed:**
- ✅ Common Module - Shared library
- ✅ Catalog Module - Products & categories
- ✅ Cart Module - Shopping cart
- ✅ Order Module - Orders & payments
- ✅ Customer Module - Authentication & profiles
- ✅ Merchant Module - Store & analytics
- ✅ REST Module - HTTP API layer
- ✅ Launcher Module - OSGI runtime

**Build Status:** ✅ SUCCESS
**Server Status:** ✅ Running on port 8080
**OSGI Platform:** ✅ All bundles ACTIVE

---

## 🆚 SpringBoot vs OSGI Comparison

| Aspect | SpringBoot | OSGI |
|--------|-----------|------|
| **Deployment** | Single JAR | Multiple bundles |
| **Modularity** | Logical (packages) | Physical (bundles) |
| **Service Access** | @Autowired | OSGI Service Registry |
| **Hot Reload** | Via DevTools | Native OSGI |
| **Testing** | Spring Test | REST APIs + OSGI console |

---

## 🚀 Next Steps

1. **For Testing:** Use cURL, Postman, or your browser to test REST APIs
2. **For Development:** Build a frontend (React, Vue) using the REST APIs
3. **For Learning:** Explore bundle lifecycle using the interactive console
4. **For Production:** Add authentication middleware, rate limiting, logging

---

## 📞 Support

- **Check bundle status:** `shopizer> status`
- **List services:** `shopizer> services`
- **View logs:** Check terminal output
- **Documentation:** See module-specific README files

---

**Last Updated:** January 13, 2026
**Course:** WIF3006 Component-Based Software Engineering
**Architecture:** OSGI Component-Based with REST API Layer

**Project Status:** ✅ Fully Operational - All features implemented and tested
