# Swagger/OpenAPI Documentation Guide

Your OSGI REST API now includes **Swagger UI** for interactive API documentation!

---

## 🎯 What is Swagger?

Swagger UI is an interactive documentation tool that allows you to:
- **Browse** all available API endpoints
- **Test** APIs directly in your browser
- **View** request/response schemas
- **Download** OpenAPI specification

**No more manual cURL commands or Postman setup!**

---

## 🚀 How to Access Swagger UI

### Step 1: Start the Server
```bash
cd com.shopizer.launcher
mvn exec:java
```

**Look for this in the console:**
```
Registered Swagger UI: http://localhost:8080/api/docs
OpenAPI Spec: http://localhost:8080/api/docs/openapi.json
```

### Step 2: Open Swagger UI in Browser

Navigate to:
```
http://localhost:8080/api/docs
```

You'll see an interactive API documentation page!

---

## 📖 Using Swagger UI

### View Available Endpoints

Swagger UI groups endpoints by module:
- **Catalog** - Products and categories
- **Cart** - Shopping cart operations
- **Order** - Order management
- **Customer** - Authentication and profiles
- **Merchant** - Store management

### Test an API Endpoint

1. **Click** on an endpoint (e.g., `POST /products`)
2. **Click** "Try it out"
3. **Edit** the request body JSON
4. **Click** "Execute"
5. **View** the response below

### Example: Create a Product

1. Navigate to **Catalog** → `POST /products`
2. Click **"Try it out"**
3. Edit the JSON:
```json
{
  "name": "Gaming Laptop",
  "description": "High-performance laptop",
  "price": 1299.99,
  "stockQuantity": 25,
  "sku": "LAP-002",
  "categoryId": 1
}
```
4. Click **"Execute"**
5. See the response with status code 201

---

## 📋 Available Endpoints in Swagger

### Catalog API
- `GET /products` - Get all products
- `POST /products` - Create product
- `GET /products/{id}` - Get product by ID
- `PUT /products/{id}` - Update product
- `DELETE /products/{id}` - Delete product
- `GET /categories` - Get all categories

### Cart API
- `GET /cart?customerId={id}` - View cart
- `POST /cart/items` - Add item to cart

### Customer API
- `POST /customers/register` - Register customer

### Order API
- `POST /orders` - Create order

**More endpoints will be added to the OpenAPI spec!**

---

## 🔧 OpenAPI Specification

### Download OpenAPI Spec
```
http://localhost:8080/api/docs/openapi.json
```

This JSON file contains the complete API specification and can be:
- Imported into Postman
- Used to generate client SDKs
- Shared with frontend developers
- Version controlled

### Use with External Tools

**Import to Postman:**
1. Open Postman
2. File → Import
3. Enter URL: `http://localhost:8080/api/docs/openapi.json`
4. Click Import

**Generate Client SDK:**
```bash
# Using OpenAPI Generator
openapi-generator-cli generate \
  -i http://localhost:8080/api/docs/openapi.json \
  -g javascript \
  -o ./client-sdk
```

---

## 🎨 Screenshots

When you open Swagger UI, you'll see:

1. **API Title & Description**
   ```
   Shopizer OSGI API
   REST API for Shopizer Component-Based E-Commerce Platform
   Version: 1.0.0
   ```

2. **Server Information**
   ```
   Base URL: http://localhost:8080/api/v1
   ```

3. **Endpoint Groups**
   - Catalog
   - Cart
   - Order
   - Customer
   - Merchant

4. **Interactive Testing**
   - Request body editor
   - Execute button
   - Response viewer

---

## 🆚 Comparison: Before vs After

### Before (Manual Testing)
```bash
# Had to manually write cURL
curl -X POST http://localhost:8080/api/v1/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":999.99,...}'
```

### After (Swagger UI)
1. Open browser
2. Click endpoint
3. Click "Try it out"
4. Edit JSON in UI
5. Click "Execute"
6. See response instantly

**Much easier!** 🎉

---

## 🔐 Future Enhancements

Currently, the Swagger implementation includes basic endpoints. You can enhance it by:

1. **Add All Endpoints** - Complete OpenAPI spec for all 67 endpoints
2. **Authentication** - Add JWT token support in Swagger UI
3. **Request Validation** - Schema validation with examples
4. **Response Examples** - Sample responses for each endpoint
5. **Error Codes** - Document all possible error responses

---

## 📁 Implementation Details

### Files Created
- `com.shopizer.rest/src/main/java/com/shopizer/rest/servlet/SwaggerServlet.java`
  - Serves Swagger UI HTML
  - Provides OpenAPI specification

### Dependencies Added
```xml
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-jaxrs2-jakarta</artifactId>
    <version>2.2.20</version>
</dependency>
```

### How It Works
```
Browser Request
    ↓
http://localhost:8080/api/docs
    ↓
SwaggerServlet.doGet()
    ↓
Serves HTML with Swagger UI JavaScript
    ↓
JavaScript fetches /api/docs/openapi.json
    ↓
SwaggerServlet returns OpenAPI spec
    ↓
Swagger UI renders interactive documentation
```

---

## 🐛 Troubleshooting

### Swagger UI Not Loading
**Check:**
1. Server is running: `http://localhost:8080/api/v1/products`
2. Console shows: "Registered Swagger UI: http://localhost:8080/api/docs"

### OpenAPI Spec Not Found
**Test directly:**
```bash
curl http://localhost:8080/api/docs/openapi.json
```

Should return JSON with API specification.

### Endpoint Not Working in Swagger
**Remember:**
- Some endpoints require query parameters (e.g., `customerId`)
- Some endpoints need ID in path (e.g., `/products/{id}`)
- POST/PUT require valid JSON request body

---

## 📚 Resources

- **Swagger UI Docs:** https://swagger.io/tools/swagger-ui/
- **OpenAPI Specification:** https://swagger.io/specification/
- **Try it Now:** http://localhost:8080/api/docs

---

## ✅ Quick Test

1. **Start server:** `mvn exec:java` (in com.shopizer.launcher)
2. **Open browser:** http://localhost:8080/api/docs
3. **Test endpoint:** Catalog → POST /products → Try it out → Execute
4. **View response:** See your newly created product!

---

**Congratulations!** You now have interactive API documentation for your OSGI platform! 🚀

No more manual cURL commands - just point, click, and test!
