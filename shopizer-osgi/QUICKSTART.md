# Shopizer OSGI - Quick Start Guide

## ✅ Current Implementation Status

### Completed Modules (2/7)

#### 1. **Common Module** (`com.shopizer.common`)
- ✅ 11 Entity classes (Product, Category, Cart, Order, Customer, Merchant, etc.)
- ✅ 4 Exception classes
- ✅ JwtTokenProvider utility
- ✅ ApiResponse DTO
- ✅ Full JPA mappings with relationships
- ✅ Maven POM with OSGI bundle configuration

#### 2. **Catalog Module** (`com.shopizer.catalog`)
- ✅ CatalogService interface (OSGI service contract)
- ✅ CatalogServiceImpl (full implementation)
- ✅ 2 Repositories (ProductRepository, CategoryRepository)
- ✅ 4 DTOs (ProductRequest/Response, CategoryRequest/Response)
- ✅ CatalogActivator (OSGI bundle activator)
- ✅ Maven POM with proper exports/imports

## 📁 Project Structure Created

```
shopizer-osgi/
│
├── pom.xml                              ✅ Parent POM (Felix 7.0.5, Java 21)
├── README.md                            ✅ Complete documentation
├── QUICKSTART.md                        ✅ This file
│
├── com.shopizer.common/                 ✅ COMPLETED
│   ├── pom.xml
│   └── src/main/java/com/shopizer/common/
│       ├── entity/                      (11 entities)
│       │   ├── Product.java
│       │   ├── Category.java
│       │   ├── Cart.java
│       │   ├── CartItem.java
│       │   ├── Order.java
│       │   ├── OrderItem.java
│       │   ├── Payment.java
│       │   ├── Customer.java
│       │   ├── Address.java
│       │   ├── Merchant.java
│       │   ├── MerchantStore.java
│       │   └── ShippingOption.java
│       ├── exception/                   (4 exceptions)
│       │   ├── ResourceNotFoundException.java
│       │   ├── BadRequestException.java
│       │   ├── InsufficientStockException.java
│       │   └── PaymentProcessingException.java
│       ├── util/
│       │   └── JwtTokenProvider.java
│       └── dto/
│           └── ApiResponse.java
│
├── com.shopizer.catalog/                ✅ COMPLETED
│   ├── pom.xml
│   └── src/main/java/com/shopizer/catalog/
│       ├── api/
│       │   └── CatalogService.java      (Service interface - 16 methods)
│       ├── impl/
│       │   └── CatalogServiceImpl.java  (Full implementation)
│       ├── repository/
│       │   ├── ProductRepository.java
│       │   └── CategoryRepository.java
│       ├── dto/
│       │   ├── ProductRequest.java
│       │   ├── ProductResponse.java
│       │   ├── CategoryRequest.java
│       │   └── CategoryResponse.java
│       └── activator/
│           └── CatalogActivator.java    (OSGI lifecycle)
│
├── com.shopizer.cart/                   🔄 TODO (folder created)
├── com.shopizer.order/                  🔄 TODO (folder created)
├── com.shopizer.customer/               🔄 TODO (folder created)
├── com.shopizer.merchant/               🔄 TODO (folder created)
└── com.shopizer.launcher/               🔄 TODO (folder created)
```

## 🚀 How to Complete Remaining Modules

### Step 1: Build Current Implementation

```bash
cd shopizer-osgi
mvn clean install
```

This will:
1. Build `com.shopizer.common` as an OSGI bundle
2. Build `com.shopizer.catalog` as an OSGI bundle
3. Generate JAR files in `target/` directories

### Step 2: Verify Bundle Generation

```bash
# Check Common bundle
ls com.shopizer.common/target/*.jar

# Check Catalog bundle
ls com.shopizer.catalog/target/*.jar

# Inspect bundle manifest
unzip -p com.shopizer.catalog/target/com.shopizer.catalog-1.0.0.jar META-INF/MANIFEST.MF
```

You should see OSGI headers like:
```
Bundle-SymbolicName: com.shopizer.catalog
Export-Package: com.shopizer.catalog.api, com.shopizer.catalog.dto
Import-Package: com.shopizer.common.entity, ...
```

## 📋 Next Steps: Implement Remaining Modules

### Priority Order:
1. **Cart Module** (depends on: Common, Catalog)
2. **Customer Module** (depends on: Common)
3. **Merchant Module** (depends on: Common, Catalog)
4. **Order Module** (depends on: Common, Cart, Catalog)
5. **Launcher Module** (orchestrates all bundles)

### Template for Each Module:

Follow the same pattern as Catalog module:

```
com.shopizer.{module}/
├── pom.xml                              (Copy from catalog, change artifactId)
└── src/main/java/com/shopizer/{module}/
    ├── api/
    │   └── {Module}Service.java         (Interface with service contract)
    ├── impl/
    │   └── {Module}ServiceImpl.java     (Implementation)
    ├── repository/
    │   └── {Entity}Repository.java      (JPA repositories)
    ├── dto/
    │   ├── {Entity}Request.java
    │   └── {Entity}Response.java
    └── activator/
        └── {Module}Activator.java       (OSGI lifecycle)
```

## 🔧 Example: Creating Cart Module

### 1. Create CartService.java
```java
package com.shopizer.cart.api;

public interface CartService {
    CartResponse addToCart(Long customerId, CartItemRequest item);
    CartResponse viewCart(Long customerId);
    CartResponse updateCart(Long customerId, Long itemId, int quantity);
    void removeFromCart(Long customerId, Long itemId);
    BigDecimal calculateTotal(Long customerId);
}
```

### 2. Create DTOs
- `CartRequest.java`
- `CartResponse.java`
- `CartItemRequest.java`
- `CartItemResponse.java`

### 3. Create Repositories
```java
package com.shopizer.cart.repository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerId(Long customerId);
}
```

### 4. Create Implementation
```java
package com.shopizer.cart.impl;

public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    // Inject CatalogService via OSGI

    // Implement all methods...
}
```

### 5. Create Activator
```java
package com.shopizer.cart.activator;

public class CartActivator implements BundleActivator {
    public void start(BundleContext context) {
        // Register CartService
        context.registerService(CartService.class, new CartServiceImpl(...), null);
    }
}
```

### 6. Update pom.xml
```xml
<artifactId>com.shopizer.cart</artifactId>
<dependencies>
    <dependency>
        <groupId>com.shopizer</groupId>
        <artifactId>com.shopizer.common</artifactId>
    </dependency>
    <dependency>
        <groupId>com.shopizer</groupId>
        <artifactId>com.shopizer.catalog</artifactId>
    </dependency>
</dependencies>
<Export-Package>
    com.shopizer.cart.api,
    com.shopizer.cart.dto
</Export-Package>
<Import-Package>
    com.shopizer.common.entity,
    com.shopizer.catalog.api,
    ...
</Import-Package>
```

## 🎯 Key Differences from SpringBoot

| Feature | SpringBoot | OSGI |
|---------|-----------|------|
| **Service Registration** | `@Service` annotation | `context.registerService()` |
| **Dependency Injection** | `@Autowired` | Service Tracker / Blueprint |
| **Configuration** | `application.properties` | Config Admin Service |
| **Module Loading** | Classpath scanning | Bundle activation order |
| **Hot Reload** | DevTools restart | Bundle update/refresh |

## 🐛 Common Issues & Solutions

### Issue 1: ClassNotFoundException
**Solution:** Ensure package is in `Export-Package` or `Import-Package`

### Issue 2: Service Not Found
**Solution:** Check service is registered in activator:
```java
serviceReg = context.registerService(MyService.class, impl, props);
```

### Issue 3: Bundle Won't Start
**Solution:** Check dependencies in pom.xml match MANIFEST.MF imports

### Issue 4: JPA Not Working
**Solution:** Add Hibernate OSGI bundle and persistence.xml

## 📊 Implementation Progress Tracker

- [x] Parent POM configuration
- [x] Common module (entities, exceptions, utilities)
- [x] Catalog module (products & categories)
- [ ] Cart module
- [ ] Customer module
- [ ] Merchant module
- [ ] Order module
- [ ] Launcher module
- [ ] Integration testing
- [ ] REST API layer (optional)

## 🎓 Learning Resources

### OSGI Basics
- Bundle lifecycle: INSTALLED → RESOLVED → STARTING → ACTIVE
- Service Registry: Publish-Subscribe pattern
- Blueprint XML: Declarative services configuration

### Key Commands (Felix Console)
```bash
lb                              # List bundles
start <bundle-id>              # Start bundle
stop <bundle-id>               # Stop bundle
services                       # List all services
inspect capability service <id> # Inspect bundle services
```

## 📞 Support

Refer to:
- [README.md](README.md) for complete architecture documentation
- PDF: `notes/Assignment 2 Report.pdf` for CBSE requirements
- SpringBoot implementation in `../springboot/` for business logic reference

---

**Current Status:** 2/7 modules completed (Common, Catalog)
**Next Action:** Implement Cart module following the template above
**Estimated Completion:** Complete all modules following the established pattern
