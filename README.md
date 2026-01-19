# Shopizer CBSE

A component-based e-commerce backend application demonstrating both **Monolithic (Spring Boot)** and **Component-Based (OSGI)** architectures for WIF3006 Component-Based Software Engineering course.

## Prerequisites

- **Java 21** - Required JDK version
- **Maven 3.9+** - For building the project
- **PostgreSQL Database** - Supabase or local PostgreSQL instance

## Project Structure

This project contains two implementations:

### 1. Spring Boot (Monolithic Architecture)
```
springboot/
├── src/main/java/com/shopizer/springboot/
│   ├── catalog/          # Product & Category management (FR-001 to FR-007)
│   ├── cart/             # Shopping cart operations (FR-008 to FR-010)
│   ├── order/            # Order processing (FR-011 to FR-014)
│   ├── merchant/         # Merchant management (FR-015 to FR-018)
│   ├── payment/          # Payment processing (FR-019 to FR-023)
│   ├── customer/         # Customer accounts (FR-024 to FR-027)
│   └── config/           # Application configuration
└── src/main/resources/
    └── application.properties  # Configuration file (not in repo)
```

### 2. OSGI (Component-Based Architecture)
```
shopizer-osgi/
├── pom.xml                      # Parent POM
├── README.md                    # Architecture documentation
├── QUICKSTART.md                # Quick start guide
├── IMPLEMENTATION_SUMMARY.md    # Complete implementation details
│
├── com.shopizer.common/         # Shared entities and utilities
│   ├── entity/                  # 11 JPA entities
│   ├── exception/               # 4 custom exceptions
│   ├── util/                    # JWT token provider
│   └── dto/                     # Common DTOs
│
├── com.shopizer.catalog/        # Product & Category (FR-001 to FR-009)
│   ├── api/                     # CatalogService interface
│   ├── impl/                    # CatalogServiceImpl
│   ├── repository/              # Product & Category repos
│   ├── dto/                     # Request/Response DTOs
│   └── activator/               # OSGI Bundle Activator
│
├── com.shopizer.cart/           # Shopping Cart (FR-010 partial)
│   ├── api/                     # CartService interface
│   ├── impl/                    # CartServiceImpl
│   ├── repository/              # Cart & CartItem repos
│   ├── dto/                     # Cart DTOs with validation
│   └── activator/               # OSGI Bundle Activator
│
├── com.shopizer.order/          # Order Processing (FR-010 to FR-023)
│   ├── api/                     # OrderService interface
│   ├── impl/                    # OrderServiceImpl
│   ├── payment/                 # PaymentProcessor interface + implementations
│   ├── repository/              # Order, OrderItem, Payment repos
│   ├── dto/                     # Order & Payment DTOs
│   └── activator/               # OSGI Bundle Activator
│
├── com.shopizer.customer/       # Customer Management (FR-024 to FR-027)
│   ├── api/                     # CustomerService interface
│   ├── impl/                    # CustomerServiceImpl
│   ├── repository/              # Customer & Address repos
│   ├── dto/                     # Customer & Auth DTOs
│   └── activator/               # OSGI Bundle Activator
│
├── com.shopizer.merchant/       # Merchant & Analytics (FR-015 to FR-018)
│   ├── api/                     # MerchantService interface
│   ├── impl/                    # MerchantServiceImpl
│   ├── repository/              # MerchantStore repo
│   ├── dto/                     # Store, Inventory, Sales DTOs
│   └── activator/               # OSGI Bundle Activator
│
└── com.shopizer.launcher/       # OSGI Framework Launcher
    └── ShpizerOSGILauncher.java # Main launcher with console
```

## Architecture Comparison

| Feature | Spring Boot (Monolithic) | OSGI (Component-Based) |
|---------|-------------------------|------------------------|
| **Modules** | 6 packages | 7 independent bundles |
| **Coupling** | Tight (direct imports) | Loose (service registry) |
| **Dependency Depth** | 3 levels | 1 level |
| **Hot Reload** | Limited | Full bundle reload |
| **Service Discovery** | Static (compile-time) | Dynamic (runtime) |
| **Deployment** | Single JAR | Individual bundles |
| **Testing** | Integration-heavy | Isolated unit tests |
| **Startup Time** | ~2 seconds | ~4 seconds |
| **Flexibility** | Lower | Higher |
| **Complexity** | Lower | Higher (OSGI learning curve) |

## Quick Start

### Spring Boot Implementation

```bash
cd springboot
mvn clean install
mvn spring-boot:run
```

Access at: http://localhost:8080/swagger-ui.html

### OSGI Implementation

```bash
cd shopizer-osgi
mvn clean install
cd com.shopizer.launcher
java -jar target/com.shopizer.launcher-1.0.0-jar-with-dependencies.jar
```

Interactive console with commands: `status`, `services`, `help`, `exit`

## Detailed Setup Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/RareZyn/shopizer-cbse.git
cd shopizer-cbse
```

### 2. Configure Database Connection

The `application.properties` file is **not included in the repository** for security reasons.

**Create the file at:** `springboot/src/main/resources/application.properties`

```properties
# Application Configuration
spring.application.name=shopizer-springboot

# Server Configuration
server.port=8080

# Database Configuration (Supabase PostgreSQL)
spring.datasource.url=jdbc:postgresql://YOUR_SUPABASE_HOST:5432/postgres
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# Swagger/OpenAPI Configuration
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.enabled=true
```

**Replace the following placeholders:**
- `YOUR_SUPABASE_HOST` - Your Supabase project database host (e.g., `db.xxxxx.supabase.co`)
- `YOUR_USERNAME` - Database username (usually `postgres`)
- `YOUR_PASSWORD` - Your Supabase database password

### 3. Build the Project

```bash
cd springboot
mvn clean install
```

### 4. Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR file:

```bash
java -jar target/springboot-0.0.1-SNAPSHOT.jar
```

## Accessing the Application

### Spring Boot Application

Once running, access the following URLs:

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Documentation | http://localhost:8080/api-docs |
| API Base URL | http://localhost:8080/api/v1 |

### OSGI Application

The OSGI implementation provides an interactive console:

```
shopizer> status      # View bundle status
shopizer> services    # List registered OSGI services
shopizer> help        # Show available commands
shopizer> exit        # Shutdown platform
```

**Services Available:**
- `com.shopizer.catalog.api.CatalogService`
- `com.shopizer.cart.api.CartService`
- `com.shopizer.order.api.OrderService`
- `com.shopizer.order.payment.PaymentProcessor` (Stripe, PayPal)
- `com.shopizer.customer.api.CustomerService`
- `com.shopizer.merchant.api.MerchantService`

## API Endpoints

### Catalog Module (FR-001 to FR-007)
- `GET /api/v1/catalog/products` - List all products
- `POST /api/v1/catalog/products` - Create product
- `GET /api/v1/catalog/products/{id}` - Get product by ID
- `PUT /api/v1/catalog/products/{id}` - Update product
- `DELETE /api/v1/catalog/products/{id}` - Delete product
- `GET /api/v1/catalog/categories` - List all categories
- `POST /api/v1/catalog/categories` - Create category

### Cart Module (FR-008 to FR-010)
- `GET /api/v1/carts` - List all carts
- `POST /api/v1/carts` - Create cart
- `GET /api/v1/carts/{id}` - Get cart by ID
- `POST /api/v1/carts/{id}/items` - Add item to cart
- `DELETE /api/v1/carts/{id}` - Delete cart

### Order Module (FR-011 to FR-014)
- `GET /api/v1/orders` - List all orders
- `POST /api/v1/orders` - Create order
- `GET /api/v1/orders/{id}` - Get order by ID
- `PUT /api/v1/orders/{id}/status` - Update order status
- `GET /api/v1/orders/customer/{customerId}` - Get orders by customer

### Merchant Module (FR-015 to FR-018)
- `GET /api/v1/merchants` - List all merchants
- `POST /api/v1/merchants` - Create merchant
- `GET /api/v1/merchants/{id}` - Get merchant by ID
- `PUT /api/v1/merchants/{id}` - Update merchant
- `DELETE /api/v1/merchants/{id}` - Delete merchant

### Payment Module (FR-019 to FR-023)
- `GET /api/v1/payments` - List all payments
- `POST /api/v1/payments` - Create payment
- `GET /api/v1/payments/{id}` - Get payment by ID
- `PUT /api/v1/payments/{id}/status` - Update payment status
- `GET /api/v1/payments/order/{orderId}` - Get payments by order

### Customer Module (FR-024 to FR-027)
- `GET /api/v1/customers` - List all customers
- `POST /api/v1/customers` - Register customer
- `GET /api/v1/customers/{id}` - Get customer by ID
- `PUT /api/v1/customers/{id}` - Update customer
- `DELETE /api/v1/customers/{id}` - Delete customer

## Functional Requirements Coverage

### Spring Boot Implementation

| Module | Requirements | Description |
|--------|--------------|-------------|
| Catalog | FR-001 to FR-007 | Product browsing, search, categories, filtering |
| Cart | FR-008 to FR-010 | Shopping cart management |
| Order | FR-011 to FR-014 | Order processing and tracking |
| Merchant | FR-015 to FR-018 | Merchant store management |
| Payment | FR-019 to FR-023 | Payment processing and history |
| Customer | FR-024 to FR-027 | Customer accounts and profiles |

### OSGI Implementation

| Module | Requirements | Status | Features |
|--------|--------------|--------|----------|
| **Common** | Foundation | ✅ Complete | 11 entities, 4 exceptions, JWT utilities |
| **Catalog** | FR-001 to FR-009 | ✅ Complete | Product/category CRUD, search, stock management |
| **Cart** | FR-010 (partial) | ✅ Complete | Add/update/remove items, validation, cart merge |
| **Order** | FR-010 to FR-023 | ✅ Complete | Order processing, payment integration, tracking |
| **Customer** | FR-024 to FR-027 | ✅ Complete | Registration, login (JWT), profile, addresses |
| **Merchant** | FR-015 to FR-018 | ✅ Complete | Store management, inventory, sales reports |
| **Launcher** | Framework | ✅ Complete | OSGI runtime, bundle management, console |

**Key OSGI Features:**
- Dynamic service registry with loose coupling
- Pluggable payment processors (Stripe, PayPal)
- Bundle lifecycle management (INSTALLED → RESOLVED → ACTIVE)
- Real-time monitoring console
- Individual module deployment

## Technology Stack

### Common Technologies
- **Language:** Java 21
- **Database:** PostgreSQL (Supabase)
- **ORM:** Spring Data JPA / Hibernate 6.4.1
- **Build Tool:** Maven 3.9+
- **Authentication:** JWT (JJWT 0.12.3)
- **Logging:** SLF4J 2.0.9

### Spring Boot Specific
- **Framework:** Spring Boot 4.0.1
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Server:** Embedded Tomcat

### OSGI Specific
- **Framework:** Apache Felix 7.0.5
- **OSGI Version:** 8.0.0
- **Service Component Runtime:** Felix SCR 2.2.6
- **Packaging:** Maven Bundle Plugin 5.1.9

## Troubleshooting

### Spring Boot Issues

#### Database Connection Issues
- Verify your Supabase credentials are correct
- Ensure your IP is allowed in Supabase network settings
- Check that the database port (5432) is accessible

#### Application Won't Start
- Ensure Java 21 is installed: `java -version`
- Verify `application.properties` exists in the correct location
- Check Maven dependencies: `mvn dependency:tree`

#### Swagger UI Not Loading
- Ensure the application is running on port 8080
- Try accessing `/swagger-ui/index.html` instead
- Check browser console for errors

### OSGI Issues

#### Bundles Won't Install
```
[FAILED] Failed to install com.shopizer.catalog
```
**Solution:** Ensure all modules are built first:
```bash
cd shopizer-osgi
mvn clean install
```

#### Bundles Won't Start
```
[FAILED] Failed to start com.shopizer.cart: Dependency not satisfied
```
**Solution:** Check bundle status with `status` command. Dependencies must be ACTIVE first.

#### Services Not Registered
**Solution:**
1. Use `status` command to verify bundles are ACTIVE
2. Check bundle activators in logs
3. Verify OSGI imports/exports in POMs

#### Framework Won't Initialize
```
FrameworkFactory not found
```
**Solution:** Ensure Apache Felix is in classpath (should be in jar-with-dependencies)

## Documentation

### OSGI Implementation
- **Architecture Guide:** [shopizer-osgi/README.md](shopizer-osgi/README.md)
- **Quick Start:** [shopizer-osgi/QUICKSTART.md](shopizer-osgi/QUICKSTART.md)
- **Implementation Details:** [shopizer-osgi/IMPLEMENTATION_SUMMARY.md](shopizer-osgi/IMPLEMENTATION_SUMMARY.md)

### Module Documentation
- [Common Module](shopizer-osgi/com.shopizer.common/README.md) - Entities and utilities
- [Catalog Module](shopizer-osgi/com.shopizer.catalog/README.md) - Product management
- [Cart Module](shopizer-osgi/com.shopizer.cart/README.md) - Shopping cart
- [Order Module](shopizer-osgi/com.shopizer.order/README.md) - Order processing
- [Customer Module](shopizer-osgi/com.shopizer.customer/README.md) - Authentication
- [Merchant Module](shopizer-osgi/com.shopizer.merchant/README.md) - Store management
- [Launcher Module](shopizer-osgi/com.shopizer.launcher/README.md) - OSGI runtime

## Project Statistics

### Spring Boot Implementation
- **Lines of Code:** ~5,000
- **Modules:** 6 packages
- **Endpoints:** 30+ REST APIs
- **Build Time:** ~30 seconds

### OSGI Implementation
- **Lines of Code:** ~10,000
- **Modules:** 7 bundles
- **Files Created:** 100+
- **Services:** 6 OSGI services
- **Build Time:** ~60 seconds
- **Startup Time:** ~4 seconds

## Key Learning Outcomes

### Component-Based Software Engineering
- ✅ Understanding modular architecture design
- ✅ Implementing loose coupling through interfaces
- ✅ Service-oriented architecture principles
- ✅ Dynamic service discovery and composition
- ✅ Lifecycle management and dependency injection

### OSGI Technology
- ✅ Bundle structure and configuration
- ✅ Service Registry pattern implementation
- ✅ Bundle Activator pattern
- ✅ ServiceTracker for dynamic dependencies
- ✅ Export/Import package declarations

### Software Design Patterns
- ✅ Repository pattern for data access
- ✅ DTO pattern for API contracts
- ✅ Strategy pattern for payment processors
- ✅ Factory pattern for service creation
- ✅ Dependency Inversion Principle

## Future Enhancements

### Planned Features
- [ ] Web API layer for OSGI implementation
- [ ] Real payment gateway integration (Stripe/PayPal SDKs)
- [ ] Implement actual sales analytics queries
- [ ] BCrypt password hashing
- [ ] Email notification service
- [ ] Product image upload and management
- [ ] Advanced search with Elasticsearch
- [ ] Caching with Redis
- [ ] Message queue for async processing

### Infrastructure
- [ ] Docker containerization
- [ ] Kubernetes deployment
- [ ] CI/CD pipeline
- [ ] Monitoring and metrics
- [ ] Load balancing
- [ ] Database migration scripts

## Contributing

This is an educational project. For questions or suggestions:
1. Open an issue on GitHub
2. Submit a pull request
3. Contact the course instructor

## License

This project is for educational purposes as part of WIF3006 Component-Based Software Engineering course.

## Acknowledgments

- **Course:** WIF3006 Component-Based Software Engineering
- **Institution:** [Your Institution Name]
- **Instructor:** [Instructor Name]
- **Technologies:** Spring Boot, Apache Felix OSGI, PostgreSQL
- **Year:** 2026
