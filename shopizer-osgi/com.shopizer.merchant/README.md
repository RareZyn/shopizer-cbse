# Merchant Module (com.shopizer.merchant)

## Overview
The Merchant module handles merchant store management, inventory tracking, sales reporting, and revenue analytics in the Shopizer e-commerce platform. It implements functional requirements FR-015 to FR-018.

## Architecture

### OSGI Bundle Configuration
- **Bundle-SymbolicName**: com.shopizer.merchant
- **Bundle-Activator**: MerchantActivator
- **Version**: 1.0.0

### Dependencies
- **com.shopizer.common** - Shared entities and utilities
- **com.shopizer.catalog** - Product management (CatalogService)
- **com.shopizer.order** - Order data for reports (OrderService)

### Service Exports
- `com.shopizer.merchant.api.MerchantService` - Merchant management interface

## Components

### 1. MerchantService Interface
Main service interface for merchant operations organized into four functional areas:

#### Store Management (FR-015)
- `createStore(MerchantStoreRequest)` - Create new store
- `getStoreById(Long)` - Get store details
- `getStoreByMerchantId(Long)` - Get store by merchant
- `updateStore(Long, MerchantStoreRequest)` - Update store
- `activateStore(Long)` - Activate store
- `deactivateStore(Long)` - Deactivate store

#### Inventory Management (FR-016)
- `getInventory(Long)` - Get all inventory items
- `getInventoryItem(Long, Long)` - Get specific item
- `updateStock(Long, Long, Integer)` - Set stock quantity
- `addStock(Long, Long, Integer)` - Increase stock
- `removeStock(Long, Long, Integer)` - Decrease stock
- `getLowStockItems(Long, Integer)` - Get items below threshold

#### Sales Reports (FR-017)
- `getSalesReport(Long, LocalDate, LocalDate)` - Comprehensive sales report
- `getDailySales(Long, LocalDate)` - Daily sales summary
- `getMonthlySales(Long, Integer, Integer)` - Monthly sales summary
- `getTopSellingProducts(Long, LocalDate, LocalDate, Integer)` - Top products

#### Revenue Analytics (FR-018)
- `getRevenueAnalytics(Long, LocalDate, LocalDate)` - Revenue analytics
- `getRevenueByCategory(Long, LocalDate, LocalDate)` - Category breakdown
- `getTotalRevenue(Long)` - Total store revenue
- `getRevenue(Long, LocalDate, LocalDate)` - Revenue for period

### 2. MerchantServiceImpl
Core implementation with:

**Store Management (FR-015)**:
- One store per merchant validation
- Store activation/deactivation
- Store profile updates (name, description, contact, address)
- Default currency (USD) and language (en)

**Inventory Management (FR-016)**:
- Real-time inventory from CatalogService
- Stock level updates (set, add, remove)
- Low stock alerts with configurable threshold
- Product ownership validation
- Integration with product catalog

**Sales Reports (FR-017)**:
- Sales summaries (daily, monthly, custom range)
- Order statistics (total, completed, cancelled)
- Revenue calculations
- Average order value
- Top selling products analysis
- TODO: Implement actual order data querying

**Revenue Analytics (FR-018)**:
- Total revenue tracking
- Average daily revenue
- Revenue growth rate calculation
- Revenue by category breakdown
- Percentage distribution analysis
- TODO: Implement actual revenue calculations

### 3. MerchantActivator
OSGI lifecycle management:
- Tracks CatalogService dependency
- Tracks OrderService dependency
- Registers MerchantService when all dependencies available
- Implements graceful shutdown

## Data Transfer Objects (DTOs)

### Request DTOs
- **MerchantStoreRequest** - Store creation/update
  - merchantId, storeName, description
  - storeEmail, storePhone
  - currency, language
  - address fields (street, city, state, country, postalCode)

### Response DTOs

#### Store Management
- **MerchantStoreResponse** - Store details
  - id, merchantId, storeName, description
  - contact info, currency, language
  - active status, address, timestamps

#### Inventory Management
- **InventoryItemResponse** - Inventory item
  - productId, productName, sku
  - stockQuantity, price
  - categoryName, active, lastUpdated

#### Sales Reports
- **SalesReportResponse** - Comprehensive sales report
  - storeId, storeName, date range
  - orderCounts (total, completed, cancelled)
  - revenue (total, average per order)
  - topProducts list

- **DailySalesResponse** - Daily summary
  - date, orderCount, totalRevenue
  - averageOrderValue, itemsSold

- **MonthlySalesResponse** - Monthly summary
  - year, month, orderCount
  - totalRevenue, averageOrderValue, itemsSold

- **ProductSalesResponse** - Product sales
  - productId, productName
  - quantitySold, totalRevenue, averagePrice

#### Revenue Analytics
- **RevenueAnalyticsResponse** - Revenue analytics
  - storeId, date range
  - totalRevenue, averageDailyRevenue
  - growthRate, revenueByCategory

- **CategoryRevenueResponse** - Category revenue
  - categoryId, categoryName
  - revenue, orderCount, itemsSold
  - percentageOfTotal

## Repositories

### MerchantStoreRepository
- `findByMerchantId(Long)` - Find store by merchant
- `findById(Long)` - Find store by ID
- `save(MerchantStore)` - Save store

## Store Management Flow (FR-015)

### Create Store
```java
MerchantStoreRequest request = new MerchantStoreRequest();
request.setMerchantId(1L);
request.setStoreName("Tech Gadgets Store");
request.setDescription("Electronics and gadgets");
request.setStoreEmail("store@techgadgets.com");
request.setStorePhone("+1234567890");
request.setCurrency("USD");
request.setLanguage("en");
request.setStreet("123 Commerce St");
request.setCity("San Francisco");
request.setState("CA");
request.setCountry("USA");
request.setPostalCode("94102");

MerchantStoreResponse store = merchantService.createStore(request);
```

### Update Store
```java
MerchantStoreRequest updateRequest = new MerchantStoreRequest();
updateRequest.setStoreName("Tech Gadgets & More");
updateRequest.setDescription("Electronics, gadgets, and accessories");

MerchantStoreResponse updated = merchantService.updateStore(storeId, updateRequest);
```

### Activate/Deactivate Store
```java
merchantService.deactivateStore(storeId); // Temporarily close store
merchantService.activateStore(storeId);   // Reopen store
```

## Inventory Management Flow (FR-016)

### View Inventory
```java
List<InventoryItemResponse> inventory = merchantService.getInventory(storeId);

for (InventoryItemResponse item : inventory) {
    System.out.println(item.getProductName() + ": " + item.getStockQuantity());
}
```

### Update Stock
```java
// Set exact quantity
merchantService.updateStock(storeId, productId, 100);

// Add stock (restock)
merchantService.addStock(storeId, productId, 50);

// Remove stock (damage, loss)
merchantService.removeStock(storeId, productId, 10);
```

### Low Stock Alerts
```java
// Get items with stock below threshold
List<InventoryItemResponse> lowStock = merchantService.getLowStockItems(storeId, 10);

for (InventoryItemResponse item : lowStock) {
    System.out.println("Low stock alert: " + item.getProductName() +
                       " (Only " + item.getStockQuantity() + " left)");
}
```

## Sales Reports Flow (FR-017)

### Generate Sales Report
```java
LocalDate startDate = LocalDate.of(2024, 1, 1);
LocalDate endDate = LocalDate.of(2024, 1, 31);

SalesReportResponse report = merchantService.getSalesReport(storeId, startDate, endDate);

System.out.println("Total Orders: " + report.getTotalOrders());
System.out.println("Total Revenue: $" + report.getTotalRevenue());
System.out.println("Average Order: $" + report.getAverageOrderValue());
```

### Daily Sales
```java
DailySalesResponse dailySales = merchantService.getDailySales(storeId, LocalDate.now());

System.out.println("Today's Sales: $" + dailySales.getTotalRevenue());
System.out.println("Orders Today: " + dailySales.getOrderCount());
```

### Monthly Sales
```java
MonthlySalesResponse monthly = merchantService.getMonthlySales(storeId, 2024, 1);

System.out.println("January 2024 Revenue: $" + monthly.getTotalRevenue());
```

### Top Selling Products
```java
List<ProductSalesResponse> topProducts = merchantService.getTopSellingProducts(
    storeId, startDate, endDate, 10
);

for (ProductSalesResponse product : topProducts) {
    System.out.println(product.getProductName() + ": " +
                       product.getQuantitySold() + " sold, $" +
                       product.getTotalRevenue() + " revenue");
}
```

## Revenue Analytics Flow (FR-018)

### Revenue Analytics
```java
RevenueAnalyticsResponse analytics = merchantService.getRevenueAnalytics(
    storeId, startDate, endDate
);

System.out.println("Total Revenue: $" + analytics.getTotalRevenue());
System.out.println("Daily Average: $" + analytics.getAverageDailyRevenue());
System.out.println("Growth Rate: " + analytics.getGrowthRate() + "%");
```

### Revenue by Category
```java
List<CategoryRevenueResponse> categoryRevenue = merchantService.getRevenueByCategory(
    storeId, startDate, endDate
);

for (CategoryRevenueResponse category : categoryRevenue) {
    System.out.println(category.getCategoryName() + ": $" +
                       category.getRevenue() +
                       " (" + category.getPercentageOfTotal() + "%)");
}
```

### Total Revenue
```java
BigDecimal totalRevenue = merchantService.getTotalRevenue(storeId);
System.out.println("All-time Revenue: $" + totalRevenue);
```

## CBSE Principles Applied

### Service-Oriented Architecture
- MerchantService exposed via OSGI service registry
- Depends on CatalogService for inventory data
- Depends on OrderService for sales/revenue data
- Interface-based contracts for all services

### Separation of Concerns
- Store management isolated from inventory
- Sales reporting separated from revenue analytics
- Each functional area has dedicated methods
- Repository layer for data access
- DTO layer for API contracts

### Loose Coupling
- Dependencies managed through OSGI ServiceTracker
- No direct coupling to catalog or order implementations
- Can function independently when dependencies available

## Error Handling

### Exceptions Thrown
- **ResourceNotFoundException**
  - MerchantStore not found
  - Product not found

- **BadRequestException**
  - Merchant already has a store
  - Product doesn't belong to store
  - Invalid quantity (negative or zero for add/remove)

## Integration Points

### CatalogService Integration
- Get all products for inventory view
- Get product details for inventory items
- Update stock quantities
- Validate product ownership

### OrderService Integration
- Query orders for sales reports
- Calculate revenue from order data
- Get order statistics (counts, status)
- Analyze top selling products

## Implementation Status

### Completed Features
- ✅ Store CRUD operations
- ✅ Store activation/deactivation
- ✅ Inventory viewing and management
- ✅ Stock updates (set, add, remove)
- ✅ Low stock alerts
- ✅ Product ownership validation

### TODO Features
- ❌ Actual order data querying from OrderService
- ❌ Sales report calculations
- ❌ Revenue analytics calculations
- ❌ Top products analysis
- ❌ Category revenue breakdown
- ❌ Growth rate calculations

The sales and revenue features are currently returning mock/empty data. Implementation requires:
1. OrderService query methods (getOrdersByStore, getOrdersByDateRange)
2. Order aggregation logic
3. Revenue calculation from order totals
4. Product sales tracking

## Future Enhancements
- Dashboard with KPIs (Key Performance Indicators)
- Profit margin analysis
- Customer analytics (acquisition, retention)
- Product performance trends
- Inventory forecasting
- Automated reorder points
- Multi-store support for single merchant
- Export reports (PDF, Excel)
- Real-time analytics with caching
- Comparison reports (period-over-period)

## Functional Requirements Coverage

| FR ID   | Requirement | Implementation |
|---------|-------------|----------------|
| FR-015  | Store management | CRUD operations, activation |
| FR-016  | Inventory management | Stock tracking, low stock alerts |
| FR-017  | Sales reports | Daily, monthly, product reports |
| FR-018  | Revenue analytics | Total, period, category revenue |

## Testing Notes

### Unit Testing
- Mock MerchantStoreRepository, CatalogService, OrderService
- Test store creation and updates
- Test inventory stock management
- Test low stock threshold filtering
- Test product ownership validation

### Integration Testing
- Test with real CatalogService
- Test inventory sync with product catalog
- Test stock updates reflecting in catalog
- Test store activation effects

### OSGI Testing
- Test bundle activation
- Test service registration
- Test dependency tracking (CatalogService, OrderService)
- Test service unavailability handling

## Security Considerations

### Authorization
- Verify merchant owns the store before operations
- Validate product belongs to store before inventory updates
- Implement role-based access (merchant vs admin)

### Data Privacy
- Restrict revenue data to store owner
- Implement audit logging for inventory changes
- Protect sensitive store information
