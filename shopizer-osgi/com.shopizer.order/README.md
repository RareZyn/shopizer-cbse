# Order Module (com.shopizer.order)

## Overview
The Order module handles order creation, payment processing, and order lifecycle management in the Shopizer e-commerce platform. It implements functional requirements FR-010 to FR-023.

## Architecture

### OSGI Bundle Configuration
- **Bundle-SymbolicName**: com.shopizer.order
- **Bundle-Activator**: OrderActivator
- **Version**: 1.0.0

### Dependencies
- **com.shopizer.common** - Shared entities and utilities
- **com.shopizer.cart** - Cart operations (CartService)
- **com.shopizer.catalog** - Product and stock management (CatalogService)

### Service Exports
- `com.shopizer.order.api.OrderService` - Main order management interface
- `com.shopizer.order.payment.PaymentProcessor` - Payment gateway component interface

## Components

### 1. OrderService Interface
Main service interface for order operations:
- `createOrder(OrderRequest)` - Create order from cart (FR-010)
- `getOrderById(Long)` - Retrieve order details (FR-011)
- `getOrderHistory(Long)` - Customer order history (FR-012)
- `updateOrderStatus(Long, String)` - Update order status (FR-013)
- `cancelOrder(Long, String)` - Cancel order with refund (FR-014)
- `processPayment(Long, PaymentRequest)` - Process payment (FR-019, FR-020)
- `getOrderDetails(Long)` - Detailed order information (FR-021)
- `trackOrder(Long)` - Order tracking information (FR-022, FR-023)

### 2. PaymentProcessor Interface (CBSE Component)
Pluggable payment gateway interface:
- `process(Order, BigDecimal)` - Process payment
- `authorize(Order, BigDecimal)` - Authorize payment
- `capture(String, BigDecimal)` - Capture authorized payment
- `refund(String, BigDecimal)` - Process refund
- `getComponentName()` - Get processor name
- `isAvailable()` - Check processor availability

**Implementations:**
- **StripePaymentProcessor** - Stripe gateway (mock)
- **PayPalPaymentProcessor** - PayPal gateway (mock)

### 3. OrderServiceImpl
Core implementation with:
- **Order Creation Workflow**:
  1. Validate cart using CartService
  2. Calculate totals and shipping
  3. Create order entity
  4. Convert cart items to order items
  5. Return pending payment order

- **Payment Processing Workflow**:
  1. Validate order status (PENDING_PAYMENT)
  2. Get payment processor by method
  3. Process payment through processor
  4. Update order status to PAID
  5. Reduce stock via CatalogService
  6. Clear customer cart

- **Order Cancellation Workflow**:
  1. Validate order can be cancelled
  2. Process refund if payment was made
  3. Restore stock to products
  4. Update order status to CANCELLED

- **Status Transition Validation**:
  - PENDING_PAYMENT → PAID or CANCELLED
  - PAID → PROCESSING or CANCELLED
  - PROCESSING → SHIPPED or CANCELLED
  - SHIPPED → DELIVERED
  - DELIVERED and CANCELLED are final states

### 4. OrderActivator
OSGI lifecycle management:
- Registers PaymentProcessor components (Stripe, PayPal)
- Tracks CartService dependency
- Tracks CatalogService dependency
- Registers OrderService when all dependencies available
- Implements graceful shutdown

## Data Transfer Objects (DTOs)

### Request DTOs
- **OrderRequest** - Order creation request
  - customerId, shippingAddress, billingAddress
  - shippingMethod, paymentMethod

- **PaymentRequest** - Payment processing request
  - paymentMethod, paymentDetails

### Response DTOs
- **OrderResponse** - Order summary
  - id, orderNumber, status, amounts, timestamps

- **OrderDetailResponse** - Complete order details
  - Full order info, items, payment, addresses

- **OrderItemResponse** - Order item details
  - productId, productName, quantity, price, subtotal

- **OrderTrackingResponse** - Tracking information
  - orderNumber, status, estimatedDelivery, shippingMethod

- **PaymentResponse** - Payment result
  - id, orderId, status, transactionId, amount

## Repositories

### OrderRepository
- `findById(Long)` - Find order by ID
- `findByCustomerIdOrderByCreatedAtDesc(Long)` - Customer order history
- `save(Order)` - Save order
- `delete(Order)` - Delete order

### OrderItemRepository
- `findByOrderId(Long)` - Get all items for order
- `save(OrderItem)` - Save order item
- `delete(OrderItem)` - Delete order item

### PaymentRepository
- `findByOrderId(Long)` - Find payment by order
- `save(Payment)` - Save payment
- `findById(Long)` - Find payment by ID

## Order Lifecycle States

```
PENDING_PAYMENT → PAID → PROCESSING → SHIPPED → DELIVERED
       ↓           ↓         ↓
    CANCELLED  CANCELLED  CANCELLED
```

### State Descriptions
- **PENDING_PAYMENT** - Order created, awaiting payment
- **PAID** - Payment successful, order confirmed
- **PROCESSING** - Order being prepared
- **SHIPPED** - Order dispatched to customer
- **DELIVERED** - Order received by customer
- **CANCELLED** - Order cancelled (with/without refund)

## Shipping Methods

| Method    | Cost   | Delivery Time |
|-----------|--------|---------------|
| STANDARD  | $5.00  | 7 days        |
| EXPRESS   | $15.00 | 3 days        |
| OVERNIGHT | $30.00 | 1 day         |

## Payment Processing Flow

1. **Create Order** (Status: PENDING_PAYMENT)
   ```java
   OrderResponse order = orderService.createOrder(request);
   ```

2. **Process Payment**
   ```java
   PaymentRequest paymentReq = new PaymentRequest();
   paymentReq.setPaymentMethod("Stripe");
   PaymentResponse payment = orderService.processPayment(orderId, paymentReq);
   ```

3. **Track Order**
   ```java
   OrderTrackingResponse tracking = orderService.trackOrder(orderId);
   ```

4. **Cancel Order** (if needed)
   ```java
   OrderResponse cancelled = orderService.cancelOrder(orderId, "Customer request");
   ```

## CBSE Principles Applied

### Component-Based Design
- **PaymentProcessor** as pluggable component interface
- Multiple implementations (Stripe, PayPal) registered as OSGI services
- Easy to add new payment gateways without modifying OrderService

### Service-Oriented Architecture
- OrderService exposed via OSGI service registry
- Loose coupling with CartService and CatalogService
- Dependencies managed through OSGI ServiceTracker

### Separation of Concerns
- Order management logic in OrderServiceImpl
- Payment processing delegated to PaymentProcessor components
- Repository layer for data access
- DTO layer for API contracts

## Error Handling

### Exceptions Thrown
- **ResourceNotFoundException** - Order/Payment not found
- **BadRequestException** - Invalid order state, invalid status transition
- **PaymentProcessingException** - Payment gateway errors
- **InsufficientStockException** - Stock validation failures (from Cart)

## Integration Points

### CartService Integration
- Validate cart before order creation
- Calculate cart total
- Get cart items for order
- Clear cart after successful payment

### CatalogService Integration
- Check product availability
- Reduce stock after payment
- Restore stock on cancellation
- Get product details for order items

## Future Enhancements
- Support for order partial refunds
- Split payment support
- Gift card/voucher integration
- Order modification before shipment
- Real payment gateway integration (Stripe SDK, PayPal SDK)
- Order notification service
- Shipping label generation
- Return/exchange management

## Functional Requirements Coverage

| FR ID   | Requirement | Implementation |
|---------|-------------|----------------|
| FR-010  | Create order from cart | `createOrder()` |
| FR-011  | View order details | `getOrderById()`, `getOrderDetails()` |
| FR-012  | Order history | `getOrderHistory()` |
| FR-013  | Update order status | `updateOrderStatus()` |
| FR-014  | Cancel order | `cancelOrder()` with stock restore |
| FR-015-018 | Merchant features | See Merchant module |
| FR-019  | Process payment | `processPayment()` |
| FR-020  | Payment methods | PaymentProcessor components |
| FR-021  | Order details | `getOrderDetails()` |
| FR-022  | Track shipment | `trackOrder()` |
| FR-023  | Delivery estimates | Calculated in `trackOrder()` |

## Testing Notes

### Unit Testing
- Mock CartService, CatalogService, and PaymentProcessor
- Test order creation workflow
- Test payment processing
- Test order cancellation with refunds
- Test status transition validation

### Integration Testing
- Test with real CartService and CatalogService
- Test stock reduction and restoration
- Test cart clearing after payment
- Test payment processor integration

### OSGI Testing
- Test bundle activation
- Test service registration
- Test dependency tracking
- Test service unavailability handling
