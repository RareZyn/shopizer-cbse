# Shopizer REST API Module

This OSGI bundle exposes HTTP REST endpoints for all Shopizer services using Jetty web server.

## Overview

The REST API module bridges OSGI services with HTTP, making them accessible via standard REST APIs. It starts an embedded Jetty server on port 8080 and automatically registers servlet endpoints for each OSGI service.

## Architecture

```
┌─────────────────────────────────────────┐
│         HTTP Clients                    │
│  (Browser, Postman, cURL, etc.)         │
└──────────────┬──────────────────────────┘
               │ HTTP REST
               ▼
┌─────────────────────────────────────────┐
│      Jetty Web Server (Port 8080)       │
│                                          │
│  ┌────────────────────────────────────┐ │
│  │  ServletContextHandler             │ │
│  │                                    │ │
│  │  ├─ CatalogServlet                │ │
│  │  ├─ CartServlet                   │ │
│  │  ├─ OrderServlet                  │ │
│  │  ├─ CustomerServlet               │ │
│  │  └─ MerchantServlet               │ │
│  └────────────────────────────────────┘ │
└──────────────┬──────────────────────────┘
               │ OSGI Service Tracker
               ▼
┌─────────────────────────────────────────┐
│         OSGI Service Layer              │
│                                          │
│  ├─ CatalogService                      │
│  ├─ CartService                         │
│  ├─ OrderService                        │
│  ├─ CustomerService                     │
│  └─ MerchantService                     │
└─────────────────────────────────────────┘
```

## Key Components

### 1. Bundle Activator
**File:** [RestActivator.java](src/main/java/com/shopizer/rest/activator/RestActivator.java)

- Starts Jetty server on port 8080
- Tracks OSGI services using ServiceTracker
- Registers servlet endpoints for each service
- Handles graceful shutdown

### 2. Base Servlet
**File:** [BaseServlet.java](src/main/java/com/shopizer/rest/servlet/BaseServlet.java)

Provides common functionality for all REST controllers:
- JSON serialization/deserialization (Jackson)
- Standard response formatting
- Error handling
- CORS support
- Path parameter extraction
- Query parameter parsing

### 3. REST Controllers

| Controller | Endpoints | Service |
|------------|-----------|---------|
| [CatalogServlet](src/main/java/com/shopizer/rest/servlet/CatalogServlet.java) | `/api/v1/products/*`<br>`/api/v1/categories/*` | CatalogService |
| [CartServlet](src/main/java/com/shopizer/rest/servlet/CartServlet.java) | `/api/v1/cart/*` | CartService |
| [OrderServlet](src/main/java/com/shopizer/rest/servlet/OrderServlet.java) | `/api/v1/orders/*` | OrderService |
| [CustomerServlet](src/main/java/com/shopizer/rest/servlet/CustomerServlet.java) | `/api/v1/customers/*` | CustomerService |
| [MerchantServlet](src/main/java/com/shopizer/rest/servlet/MerchantServlet.java) | `/api/v1/merchants/*` | MerchantService |

## Dependencies

### OSGI Framework
- `osgi.core` (8.0.0) - OSGI runtime
- `osgi.cmpn` (7.0.0) - OSGI compendium services

### Web Server
- `jetty-server` (11.0.18) - HTTP server
- `jetty-servlet` (11.0.18) - Servlet container
- `jakarta.servlet-api` (6.0.0) - Servlet API

### JSON Processing
- `jackson-databind` (2.16.0) - JSON serialization
- `jackson-datatype-jsr310` (2.16.0) - Java 8 date/time support

### Shopizer Modules
- All 6 application bundles (common, catalog, cart, order, customer, merchant)

## Building

```bash
cd shopizer-osgi/com.shopizer.rest
mvn clean install
```

This creates: `target/com.shopizer.rest-1.0.0.jar`

## Running

The REST module is automatically loaded by the launcher:

```bash
cd shopizer-osgi/com.shopizer.launcher
mvn exec:java
```

Expected console output:
```
[STARTED] com.shopizer.rest
Jetty server started on port 8080
REST API available at: http://localhost:8080/api/v1
```

## Testing

### Quick Health Check
```bash
curl http://localhost:8080/api/v1/products
```

### Create a Product
```bash
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Product",
    "price": 99.99,
    "stockQuantity": 100,
    "sku": "TEST-001"
  }'
```

### View All Products
```bash
curl http://localhost:8080/api/v1/products
```

**For comprehensive testing guide, see:** [REST_API_TESTING_GUIDE.md](../REST_API_TESTING_GUIDE.md)

## API Endpoints Summary

### Catalog API
- `GET /api/v1/products` - List all products
- `POST /api/v1/products` - Create product
- `GET /api/v1/products/{id}` - Get product
- `PUT /api/v1/products/{id}` - Update product
- `DELETE /api/v1/products/{id}` - Delete product
- `GET /api/v1/products/search?q={keyword}` - Search products
- `GET /api/v1/categories` - List all categories

### Cart API
- `POST /api/v1/cart/items` - Add to cart
- `GET /api/v1/cart?customerId={id}` - View cart
- `PUT /api/v1/cart/items/{id}` - Update quantity
- `DELETE /api/v1/cart/items/{id}` - Remove item
- `POST /api/v1/cart/validate` - Validate cart

### Order API
- `POST /api/v1/orders` - Create order
- `GET /api/v1/orders/{id}` - Get order
- `GET /api/v1/orders/history?customerId={id}` - Order history
- `POST /api/v1/orders/{id}/payment` - Process payment
- `GET /api/v1/orders/{id}/tracking` - Track order

### Customer API
- `POST /api/v1/customers/register` - Register
- `POST /api/v1/customers/login` - Login
- `GET /api/v1/customers/{id}` - Get profile
- `PUT /api/v1/customers/{id}` - Update profile
- `POST /api/v1/customers/{id}/addresses` - Add address

### Merchant API
- `POST /api/v1/merchants/stores` - Create store
- `GET /api/v1/merchants/{id}/inventory` - View inventory
- `GET /api/v1/merchants/{id}/reports/sales` - Sales report
- `GET /api/v1/merchants/{id}/analytics/revenue` - Revenue analytics

## Configuration

### Changing HTTP Port

Edit [RestActivator.java:20](src/main/java/com/shopizer/rest/activator/RestActivator.java#L20):
```java
private static final int HTTP_PORT = 8080;  // Change to desired port
```

### CORS Settings

CORS is enabled by default in [BaseServlet.java](src/main/java/com/shopizer/rest/servlet/BaseServlet.java#L115):
```java
response.setHeader("Access-Control-Allow-Origin", "*");
response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
```

For production, restrict origins:
```java
response.setHeader("Access-Control-Allow-Origin", "https://yourdomain.com");
```

## OSGI Manifest

**Bundle-SymbolicName:** `com.shopizer.rest`
**Bundle-Activator:** `com.shopizer.rest.activator.RestActivator`

**Exported Packages:**
- `com.shopizer.rest.api`
- `com.shopizer.rest.servlet`

**Imported Packages:**
- OSGI services (Catalog, Cart, Order, Customer, Merchant)
- Jakarta Servlet API
- Jackson JSON
- SLF4J logging

**Embedded Dependencies:**
- Jetty server and all dependencies
- Jackson JSON libraries

## Troubleshooting

### Port Already in Use
```
Error: Address already in use: bind
```

**Solution:** Kill process using port 8080 or change `HTTP_PORT` in RestActivator.

### Service Not Available
```
WARN: CatalogService not available, skipping endpoint registration
```

**Solution:** Ensure all application bundles started successfully. Check with:
```
shopizer> status
```

### JSON Serialization Error
```
Error: Could not read JSON
```

**Solution:** Verify request body is valid JSON and Content-Type header is set:
```bash
-H "Content-Type: application/json"
```

## Future Enhancements

1. **Authentication/Authorization**
   - JWT token validation middleware
   - Role-based access control (RBAC)
   - OAuth2 integration

2. **API Documentation**
   - OpenAPI/Swagger integration
   - Auto-generated API docs at `/api/docs`

3. **Rate Limiting**
   - Request throttling per client
   - DDoS protection

4. **Caching**
   - HTTP cache headers
   - ETag support for product catalog

5. **WebSocket Support**
   - Real-time order updates
   - Live inventory notifications

## References

- [Jetty Documentation](https://www.eclipse.org/jetty/documentation/)
- [Jakarta Servlet Specification](https://jakarta.ee/specifications/servlet/)
- [Jackson JSON Documentation](https://github.com/FasterXML/jackson)
- [OSGI Compendium HTTP Service](https://docs.osgi.org/specification/osgi.cmpn/8.1.0/service.http.html)

## License

Part of Shopizer OSGI - Component-Based E-Commerce Platform
