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

### 7. Launcher Module (com.shopizer.launcher) ✅ COMPLETED
**Purpose:** OSGI runtime initialization and bundle management

**Responsibilities:**
- Bootstrap Apache Felix OSGI framework
- Install and start all bundles in correct order
- Manage bundle lifecycle
- Provide entry point for application
- Install required third-party OSGI bundles (Jakarta Persistence, SLF4J, ASM, Aries SPI Fly)
- Interactive console for monitoring bundle status and services

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

### Using Launcher Module (Recommended) ✅
```bash
cd com.shopizer.launcher
mvn exec:java
```

**Interactive Console Commands:**
- `status` - Display status of all bundles
- `services` - List all registered OSGI services
- `help` - Show available commands
- `exit` - Shutdown the OSGI platform

**Expected Output:**
```
========================================
   Shopizer OSGI Platform Launcher
========================================
[OK] OSGI Framework started

Installing third-party dependency bundles...
[INSTALLED] jakarta.persistence-api-3.1.0.jar
[INSTALLED] asm-9.7.jar
[INSTALLED] org.apache.aries.spifly.dynamic.bundle-1.3.7.jar
[INSTALLED] slf4j-api-2.0.9.jar
[INSTALLED] slf4j-simple-2.0.9.jar

Installing application bundles...
[INSTALLED] com.shopizer.common
[INSTALLED] com.shopizer.catalog
[INSTALLED] com.shopizer.cart
[INSTALLED] com.shopizer.order
[INSTALLED] com.shopizer.customer
[INSTALLED] com.shopizer.merchant

Starting bundles...
[STARTED] org.apache.aries.spifly.dynamic.bundle
[STARTED] jakarta.persistence-api
[STARTED] slf4j.api
[STARTED] slf4j.simple
[STARTED] com.shopizer.common
[STARTED] com.shopizer.catalog
[STARTED] com.shopizer.cart
[STARTED] com.shopizer.order
[STARTED] com.shopizer.customer
[STARTED] com.shopizer.merchant
========================================
   Shopizer OSGI Platform Running
========================================
shopizer>
```

### Manual Felix Container (Alternative)
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

## Implementation Status

### Completed Modules:
1. ✅ **Common Module** - Shared entities, utilities, exceptions
   - All JPA entities defined and annotated
   - JWT token provider utility
   - Common exception classes
   - Embedded dependencies (Jakarta Persistence, Hibernate, Spring Data JPA, JJWT)
   - Exports all required packages for other modules

2. ✅ **Catalog Module** - Product and category management
   - CatalogService interface and implementation
   - Product CRUD operations
   - Category management
   - Service registration in OSGI registry

3. ✅ **Cart Module** - Shopping cart operations
   - CartService interface and implementation
   - Add/update/remove cart items
   - Cart validation
   - Service registration in OSGI registry

4. ✅ **Order Module** - Order processing and payment
   - OrderService interface and implementation
   - PaymentProcessor interface and implementation
   - Order creation and management
   - Payment processing
   - Service registration in OSGI registry

5. ✅ **Customer Module** - Customer account management
   - CustomerService interface and implementation
   - Registration and authentication
   - Profile management
   - Address management
   - Service registration in OSGI registry

6. ✅ **Merchant Module** - Store and inventory management
   - MerchantService interface and implementation
   - Store management
   - Inventory management
   - Service registration in OSGI registry

7. ✅ **Launcher Module** - OSGI runtime and lifecycle management
   - Apache Felix framework integration
   - Automatic third-party bundle installation
   - Bundle dependency resolution
   - Interactive console for monitoring
   - All bundles successfully start and run

### Recent Fixes and Improvements:

**Maven Build Configuration:**
- ✅ Added exec-maven-plugin with mainClass configuration
- ✅ Configured maven-bundle-plugin for proper OSGI manifest generation
- ✅ Added all required third-party dependencies (ASM, Aries SPI Fly, SLF4J)

**OSGI Bundle Configuration:**
- ✅ Resolved Jakarta Persistence API bundle dependencies
- ✅ Configured SLF4J with ServiceLoader support via Aries SPI Fly
- ✅ Added ASM library bundles (all modules: asm, asm-commons, asm-tree, asm-analysis, asm-util)
- ✅ Configured bundle to embed and export Spring Framework packages
- ✅ Fixed all Import-Package and Export-Package declarations

**Bundle Lifecycle:**
- ✅ All bundles install successfully
- ✅ All bundles resolve dependencies correctly
- ✅ All bundles start and reach ACTIVE state
- ✅ OSGI services properly registered and discoverable

## Technical Implementation Details

### Third-Party Bundle Management

The launcher automatically installs and starts required third-party OSGI bundles:

1. **Jakarta Persistence API 3.1.0** - JPA specification
2. **ASM 9.7** (5 modules) - Bytecode manipulation for OSGI ServiceLoader
   - asm-9.7.jar
   - asm-commons-9.7.jar
   - asm-tree-9.7.jar
   - asm-analysis-9.7.jar
   - asm-util-9.7.jar
3. **Apache Aries SPI Fly 1.3.7** - OSGI ServiceLoader mediator
4. **SLF4J 2.0.9** - Logging facade
   - slf4j-api-2.0.9.jar
   - slf4j-simple-2.0.9.jar

### Bundle Configuration Strategy

**Common Module (Shared Library Pattern):**
- Embeds all transitive dependencies (Hibernate, Spring Data JPA, JJWT, Jakarta Persistence)
- Exports packages from embedded JARs for consumption by other bundles
- Uses `Embed-Dependency` and `Bundle-ClassPath` for dependency inclusion
- Configured with `resolution:=optional` for flexible imports

**Application Bundles:**
- Import packages from common module
- Import OSGI framework packages
- Export service interfaces and DTOs
- Private implementation packages

### OSGI Manifest Configuration

Key manifest headers generated by maven-bundle-plugin:

```
Bundle-SymbolicName: com.shopizer.common
Export-Package:
  com.shopizer.common.entity,
  com.shopizer.common.exception,
  com.shopizer.common.util,
  com.shopizer.common.dto,
  jakarta.persistence.*,
  org.hibernate.*,
  org.springframework.*,
  io.jsonwebtoken.*
Import-Package:
  org.slf4j;version="[2.0,3)",
  org.osgi.framework;version="[1.8,2)",
  *;resolution:=optional
Bundle-ClassPath: .,{maven-dependencies}
```

## Troubleshooting

### Bundle Not Starting
- Check OSGI console: Use `status` command in shopizer console
- Verify all dependencies are satisfied
- Check for missing Import-Package declarations
- Ensure bundles are started in correct order (dependencies first)

### Common Issues and Solutions

**Issue:** `missing requirement osgi.wiring.package`
- **Cause:** Required package not exported by any bundle
- **Solution:** Ensure dependency bundles are installed and export the package

**Issue:** `Bundle symbolic name and version are not unique`
- **Cause:** Attempting to install the same bundle twice
- **Solution:** Restart the OSGI framework or uninstall the duplicate bundle

**Issue:** `No SLF4J providers were found`
- **Cause:** SLF4J ServiceLoader not properly configured (warning only)
- **Solution:** This is a known limitation; logging falls back to NOP (no-operation) logger
- **Impact:** Minimal - bundles still function correctly

**Issue:** Maven exec:java fails with `mainClass` parameter missing
- **Cause:** exec-maven-plugin not configured
- **Solution:** Already fixed - mainClass configured in launcher pom.xml

### Service Not Found
- Verify service is registered: Use `services` command
- Check Import-Package in MANIFEST.MF (target/classes/META-INF/MANIFEST.MF)
- Ensure dependent bundles are in ACTIVE state
- Use `status` command to check bundle states

### Build Issues
- **Clean build:** `mvn clean install -DskipTests`
- **Rebuild single module:** Navigate to module directory and run `mvn clean install`
- **Clear Maven cache:** Delete `~/.m2/repository/com/shopizer/`

## Resources

- [Apache Felix Documentation](https://felix.apache.org/documentation.html)
- [OSGI Alliance Specifications](https://www.osgi.org/specifications/)
- [Spring Data JPA in OSGI](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

## License

This project is part of an academic assignment for WIF3006 Component-Based Software Engineering.

---

**Status:** ✅ Fully Operational
**Completed Modules:** 7/7 (All modules completed and running)
**All Bundles:** Successfully installed, resolved, and started
**OSGI Platform:** Apache Felix running with interactive console

### Project Achievements:
- ✅ Full OSGI modular architecture implementation
- ✅ Component-based design with service-oriented architecture
- ✅ Proper dependency management and bundle lifecycle
- ✅ Maven build automation with bundle packaging
- ✅ Third-party library integration in OSGI environment
- ✅ Interactive monitoring and management console
- ✅ All 6 application bundles + launcher successfully running

**Last Updated:** 2026-01-13
