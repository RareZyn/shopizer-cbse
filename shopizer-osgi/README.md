# Shopizer OSGI Architecture

Component-Based E-Commerce Platform using OSGI framework.

## Project Structure

```
shopizer-osgi/
├── pom.xml                          # Parent POM
├── com.shopizer.common/             # ✅ Common Module (Entities, Utilities, Exceptions)
├── com.shopizer.catalog/            # ✅ Catalog Module (Products & Categories)
├── com.shopizer.cart/               # Cart Module (Shopping Cart)
├── com.shopizer.order/              # Order Module (Orders & Payments)
├── com.shopizer.customer/           # Customer Module (Customer Management)
├── com.shopizer.merchant/           # Merchant Module (Store & Inventory)
└── com.shopizer.launcher/           # Launcher Module (OSGI Runtime)
```

## Module Overview

### 1. Common Module (com.shopizer.common) ✅ COMPLETED
**Purpose:** Shared entities, utilities, exceptions, and DTOs

**Exports:**
- `com.shopizer.common.entity` - JPA Entities (Product, Category, Cart, Order, Customer, Merchant, etc.)
- `com.shopizer.common.exception` - Custom exceptions
- `com.shopizer.common.util` - Utilities (JwtTokenProvider)
- `com.shopizer.common.dto` - Shared DTOs (ApiResponse)

**Entities:**
- Product.java
- Category.java
- Cart.java, CartItem.java
- Order.java, OrderItem.java
- Payment.java
- Customer.java, Address.java
- Merchant.java, MerchantStore.java, ShippingOption.java

### 2. Catalog Module (com.shopizer.catalog) ✅ COMPLETED
**Purpose:** Product and category management (FR-001 to FR-005)

**OSGI Service Interface:** `CatalogService`

**Operations:**
- Product CRUD (Create, Read, Update, Delete)
- Category management
- Product search
- Stock management

**Exports:**
- `com.shopizer.catalog.api` - Service interface
- `com.shopizer.catalog.dto` - DTOs (ProductRequest/Response, CategoryRequest/Response)

**Imports:**
- `com.shopizer.common.entity`
- `com.shopizer.common.exception`

### 3. Cart Module (com.shopizer.cart) 🔄 TODO
**Purpose:** Shopping cart operations (FR-006 to FR-009)

**OSGI Service Interface:** `CartService`

**Operations:**
- Add to cart
- View cart
- Update cart items
- Calculate totals
- Cart validation

**Dependencies:**
- Imports: `com.shopizer.catalog.api.CatalogService` (for product validation)

### 4. Order Module (com.shopizer.order) 🔄 TODO
**Purpose:** Order processing and payment (FR-010 to FR-023)

**OSGI Service Interfaces:**
- `OrderService` - Order management
- `PaymentProcessor` - Payment gateway interface

**Operations:**
- Create order
- Process payment
- Order status management
- Order history
- Payment integration (Stripe, PayPal)

**Dependencies:**
- Imports: `com.shopizer.cart.api.CartService`
- Imports: `com.shopizer.catalog.api.CatalogService`

### 5. Customer Module (com.shopizer.customer) 🔄 TODO
**Purpose:** Customer account management (FR-024 to FR-027)

**OSGI Service Interface:** `CustomerService`

**Operations:**
- Registration
- Login/Authentication
- Profile management
- Address management
- Order history

**Dependencies:**
- Imports: `com.shopizer.common.util.JwtTokenProvider`
- Imports: `com.shopizer.order.api.OrderService`

### 6. Merchant Module (com.shopizer.merchant) 🔄 TODO
**Purpose:** Store and inventory management (FR-015 to FR-018)

**OSGI Service Interface:** `MerchantService`

**Operations:**
- Store creation and management
- Inventory management
- Product management
- Sales reports
- Shipping options

**Dependencies:**
- Imports: `com.shopizer.catalog.api.CatalogService`

### 7. Launcher Module (com.shopizer.launcher) 🔄 TODO
**Purpose:** OSGI runtime initialization and bundle management

**Responsibilities:**
- Bootstrap Apache Felix OSGI framework
- Install and start all bundles in correct order
- Manage bundle lifecycle
- Provide entry point for application

## Building the Project

### Prerequisites
- Java 21
- Maven 3.x
- PostgreSQL (for database)

### Build All Modules
```bash
cd shopizer-osgi
mvn clean install
```

### Build Individual Module
```bash
cd com.shopizer.catalog
mvn clean install
```

## Running the Application

### Option 1: Using Launcher Module
```bash
cd com.shopizer.launcher
mvn exec:java
```

### Option 2: Manual Felix Container
```bash
java -jar org.apache.felix.main-7.0.5.jar
```

Then install bundles manually:
```
felix> install file:///path/to/com.shopizer.common-1.0.0.jar
felix> install file:///path/to/com.shopizer.catalog-1.0.0.jar
felix> start <bundle-id>
```

## OSGI Service Communication

### Service Registration
Each module registers its service interface in the OSGI registry:

```java
// In CatalogActivator.java
CatalogService catalogService = new CatalogServiceImpl(...);
context.registerService(CatalogService.class, catalogService, properties);
```

### Service Consumption
Other modules consume services via OSGI service tracker:

```java
// In CartServiceImpl.java
ServiceTracker<CatalogService, CatalogService> tracker =
    new ServiceTracker<>(context, CatalogService.class, null);
tracker.open();
CatalogService catalogService = tracker.getService();
```

## Component Dependencies

```
┌─────────────┐
│  Launcher   │
└──────┬──────┘
       │ initializes
       │
       ├──> Common Module (Base Library)
       │
       ├──> Catalog Module
       │    └─> depends on: Common
       │
       ├──> Cart Module
       │    └─> depends on: Common, Catalog
       │
       ├──> Customer Module
       │    └─> depends on: Common, Order
       │
       ├──> Merchant Module
       │    └─> depends on: Common, Catalog
       │
       └──> Order Module
            └─> depends on: Common, Cart, Catalog
```

## Database Configuration

### PostgreSQL (Supabase)
Update configuration in each module's `persistence.xml` or via OSGI Config Admin:

```properties
javax.persistence.jdbc.url=jdbc:postgresql://localhost:5432/shopizer
javax.persistence.jdbc.user=your_username
javax.persistence.jdbc.password=your_password
javax.persistence.jdbc.driver=org.postgresql.Driver
hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
hibernate.hbm2ddl.auto=update
```

## Comparison: SpringBoot vs OSGI

| Aspect | SpringBoot | OSGI |
|--------|-----------|------|
| **Deployment** | Single JAR | Multiple bundles |
| **Dependency Injection** | Spring @Autowired | OSGI Service Registry |
| **Modularity** | Logical (packages) | Physical (bundles) |
| **Lifecycle** | Application-wide | Per-bundle |
| **Dependency Depth** | 3 levels | 1 level |
| **Independent Deployment** | No | Yes |
| **Hot Reload** | Via DevTools | Native OSGI feature |

## Next Steps

### Remaining Modules to Implement:
1. ✅ Common Module - COMPLETED
2. ✅ Catalog Module - COMPLETED
3. 🔄 Cart Module - TODO
4. 🔄 Order Module - TODO
5. 🔄 Customer Module - TODO
6. 🔄 Merchant Module - TODO
7. 🔄 Launcher Module - TODO

### Implementation Pattern for Remaining Modules:

For each module, create:
1. **API Package** - Service interface (exported)
2. **DTO Package** - Request/Response objects (exported)
3. **Impl Package** - Service implementation (private)
4. **Repository Package** - JPA repositories (private)
5. **Activator Package** - OSGI bundle activator (private)
6. **pom.xml** - Maven configuration with bundle plugin
7. **MANIFEST.MF** - Generated by maven-bundle-plugin

### Example Module Creation Workflow:

```bash
# 1. Create module structure
mkdir -p com.shopizer.cart/src/main/java/com/shopizer/cart/{api,impl,repository,dto,activator}
mkdir -p com.shopizer.cart/src/main/resources/OSGI-INF

# 2. Create service interface
# api/CartService.java

# 3. Create DTOs
# dto/CartRequest.java, CartResponse.java

# 4. Create repositories
# repository/CartRepository.java

# 5. Create service implementation
# impl/CartServiceImpl.java

# 6. Create activator
# activator/CartActivator.java

# 7. Create pom.xml with proper exports/imports

# 8. Build
mvn clean install
```

## Troubleshooting

### Bundle Not Starting
- Check OSGI console: `felix> lb` (list bundles)
- Check bundle status: `felix> inspect capability service <bundle-id>`
- View logs for errors during activation

### Service Not Found
- Verify service is registered: `felix> services`
- Check Import-Package in MANIFEST.MF
- Ensure dependent bundles are started

### JPA Issues
- Verify persistence.xml location
- Check Hibernate OSGI dependencies
- Ensure database driver bundle is installed

## Resources

- [Apache Felix Documentation](https://felix.apache.org/documentation.html)
- [OSGI Alliance Specifications](https://www.osgi.org/specifications/)
- [Spring Data JPA in OSGI](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

## License

This project is part of an academic assignment for WIF3006 Component-Based Software Engineering.

---

**Status:** In Development
**Completed Modules:** 2/7 (Common, Catalog)
**Next Milestone:** Complete Cart, Order, Customer, and Merchant modules
