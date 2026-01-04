# Shopizer CBSE

A component-based e-commerce backend application built with Spring Boot 4.0.1 and Java 21 for WIF3006 Component-Based Software Engineering course.

## Prerequisites

- **Java 21** - Required JDK version
- **Maven 3.9+** - For building the project
- **PostgreSQL Database** - Supabase or local PostgreSQL instance

## Project Structure

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

## Setup Instructions

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

Once running, access the following URLs:

| Resource | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Documentation | http://localhost:8080/api-docs |
| API Base URL | http://localhost:8080/api/v1 |

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

| Module | Requirements | Description |
|--------|--------------|-------------|
| Catalog | FR-001 to FR-007 | Product browsing, search, categories, filtering |
| Cart | FR-008 to FR-010 | Shopping cart management |
| Order | FR-011 to FR-014 | Order processing and tracking |
| Merchant | FR-015 to FR-018 | Merchant store management |
| Payment | FR-019 to FR-023 | Payment processing and history |
| Customer | FR-024 to FR-027 | Customer accounts and profiles |

## Technology Stack

- **Framework:** Spring Boot 4.0.1
- **Language:** Java 21
- **Database:** PostgreSQL (Supabase)
- **ORM:** Spring Data JPA / Hibernate
- **Documentation:** SpringDoc OpenAPI (Swagger)
- **Build Tool:** Maven

## Troubleshooting

### Database Connection Issues
- Verify your Supabase credentials are correct
- Ensure your IP is allowed in Supabase network settings
- Check that the database port (5432) is accessible

### Application Won't Start
- Ensure Java 21 is installed: `java -version`
- Verify `application.properties` exists in the correct location
- Check Maven dependencies: `mvn dependency:tree`

### Swagger UI Not Loading
- Ensure the application is running on port 8080
- Try accessing `/swagger-ui/index.html` instead
- Check browser console for errors

## License

This project is for educational purposes as part of WIF3006 Component-Based Software Engineering course.
