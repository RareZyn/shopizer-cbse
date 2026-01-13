package com.shopizer.order.api;

import com.shopizer.order.dto.*;
import java.util.List;

/**
 * OSGI Service Interface for Order Module
 * Provides order processing operations (FR-010 to FR-014)
 */
public interface OrderService {

    /**
     * Create order from cart (FR-010)
     * @param request Order creation request
     * @return Created order response
     */
    OrderResponse createOrder(OrderRequest request);

    /**
     * Get order by ID (FR-011)
     * @param orderId Order ID
     * @return Order details
     */
    OrderResponse getOrderById(Long orderId);

    /**
     * Get customer's order history (FR-012)
     * @param customerId Customer ID
     * @return List of orders
     */
    List<OrderResponse> getOrderHistory(Long customerId);

    /**
     * Update order status (FR-013)
     * @param orderId Order ID
     * @param status New status
     * @return Updated order
     */
    OrderResponse updateOrderStatus(Long orderId, String status);

    /**
     * Cancel order (FR-014)
     * @param orderId Order ID
     * @param reason Cancellation reason
     * @return Updated order
     */
    OrderResponse cancelOrder(Long orderId, String reason);

    /**
     * Process payment for order
     * @param orderId Order ID
     * @param paymentRequest Payment details
     * @return Payment response
     */
    PaymentResponse processPayment(Long orderId, PaymentRequest paymentRequest);

    /**
     * Get order with items
     * @param orderId Order ID
     * @return Order with full item details
     */
    OrderDetailResponse getOrderDetails(Long orderId);

    /**
     * Track order status
     * @param orderId Order ID
     * @return Order tracking information
     */
    OrderTrackingResponse trackOrder(Long orderId);
}
