package com.shopizer.order.impl;

import com.shopizer.cart.api.CartService;
import com.shopizer.catalog.api.CatalogService;
import com.shopizer.common.entity.*;
import com.shopizer.common.exception.BadRequestException;
import com.shopizer.common.exception.PaymentProcessingException;
import com.shopizer.common.exception.ResourceNotFoundException;
import com.shopizer.order.api.OrderService;
import com.shopizer.order.dto.*;
import com.shopizer.order.payment.PaymentProcessor;
import com.shopizer.order.repository.OrderItemRepository;
import com.shopizer.order.repository.OrderRepository;
import com.shopizer.order.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final CartService cartService;
    private final CatalogService catalogService;
    private final List<PaymentProcessor> paymentProcessors;

    public OrderServiceImpl(OrderRepository orderRepository,
                           OrderItemRepository orderItemRepository,
                           PaymentRepository paymentRepository,
                           CartService cartService,
                           CatalogService catalogService,
                           List<PaymentProcessor> paymentProcessors) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.cartService = cartService;
        this.catalogService = catalogService;
        this.paymentProcessors = paymentProcessors;
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        logger.info("Creating order for customer: {}", request.getCustomerId());

        // Validate cart
        var cartValidation = cartService.validateCart(request.getCustomerId());
        if (!cartValidation.isValid()) {
            throw new BadRequestException("Cart validation failed: " +
                String.join(", ", cartValidation.getErrors()));
        }

        // Get cart total
        BigDecimal cartTotal = cartService.calculateTotal(request.getCustomerId());

        // Calculate shipping
        BigDecimal shippingCost = calculateShipping(request.getShippingMethod());

        // Calculate total
        BigDecimal totalAmount = cartTotal.add(shippingCost);

        // Create order entity
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());

        Customer customer = new Customer();
        customer.setId(request.getCustomerId());
        order.setCustomer(customer);

        order.setStatus("PENDING_PAYMENT");
        order.setSubtotal(cartTotal);
        order.setShippingCost(shippingCost);
        order.setTotalAmount(totalAmount);

        // Set shipping address
        Address shippingAddress = new Address();
        shippingAddress.setStreet(request.getShippingAddress().getStreet());
        shippingAddress.setCity(request.getShippingAddress().getCity());
        shippingAddress.setState(request.getShippingAddress().getState());
        shippingAddress.setCountry(request.getShippingAddress().getCountry());
        shippingAddress.setPostalCode(request.getShippingAddress().getPostalCode());
        order.setShippingAddress(shippingAddress);

        // Set billing address
        if (request.getBillingAddress() != null) {
            Address billingAddress = new Address();
            billingAddress.setStreet(request.getBillingAddress().getStreet());
            billingAddress.setCity(request.getBillingAddress().getCity());
            billingAddress.setState(request.getBillingAddress().getState());
            billingAddress.setCountry(request.getBillingAddress().getCountry());
            billingAddress.setPostalCode(request.getBillingAddress().getPostalCode());
            order.setBillingAddress(billingAddress);
        } else {
            order.setBillingAddress(shippingAddress);
        }

        order.setShippingMethod(request.getShippingMethod());
        order.setPaymentMethod(request.getPaymentMethod());

        // Save order
        order = orderRepository.save(order);

        // Create order items from cart
        var cartResponse = cartService.viewCart(request.getCustomerId());
        for (var cartItem : cartResponse.getItems()) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);

            Product product = new Product();
            product.setId(cartItem.getProductId());
            orderItem.setProduct(product);

            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setSubtotal(cartItem.getSubtotal());

            orderItemRepository.save(orderItem);
        }

        logger.info("Order created successfully: {}", order.getOrderNumber());

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse getOrderById(Long orderId) {
        logger.info("Fetching order: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getOrderHistory(Long customerId) {
        logger.info("Fetching order history for customer: {}", customerId);

        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);

        return orders.stream()
            .map(this::mapToOrderResponse)
            .collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        logger.info("Updating order {} status to {}", orderId, status);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Validate status transition
        validateStatusTransition(order.getStatus(), status);

        order.setStatus(status);
        order = orderRepository.save(order);

        logger.info("Order status updated successfully");

        return mapToOrderResponse(order);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, String reason) {
        logger.info("Cancelling order: {}, reason: {}", orderId, reason);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Check if order can be cancelled
        if (order.getStatus().equals("SHIPPED") || order.getStatus().equals("DELIVERED")) {
            throw new BadRequestException("Cannot cancel order that has been shipped or delivered");
        }

        if (order.getStatus().equals("CANCELLED")) {
            throw new BadRequestException("Order is already cancelled");
        }

        // Process refund if payment was made
        if (order.getStatus().equals("PAID") || order.getStatus().equals("PROCESSING")) {
            Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", orderId));

            if (payment.getStatus().equals("SUCCESS")) {
                PaymentProcessor processor = getPaymentProcessor(payment.getPaymentMethod());
                try {
                    String refundId = processor.refund(payment.getTransactionId(), order.getTotalAmount());
                    payment.setStatus("REFUNDED");
                    payment.setTransactionId(refundId);
                    paymentRepository.save(payment);
                    logger.info("Refund processed successfully: {}", refundId);
                } catch (Exception e) {
                    logger.error("Refund failed for order: {}", orderId, e);
                    throw new PaymentProcessingException("Refund processing failed: " + e.getMessage(), e);
                }
            }
        }

        // Restore stock
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            try {
                catalogService.updateStock(item.getProduct().getId(), item.getQuantity());
                logger.info("Stock restored for product: {}, quantity: {}",
                    item.getProduct().getId(), item.getQuantity());
            } catch (Exception e) {
                logger.error("Failed to restore stock for product: {}", item.getProduct().getId(), e);
            }
        }

        order.setStatus("CANCELLED");
        order = orderRepository.save(order);

        logger.info("Order cancelled successfully");

        return mapToOrderResponse(order);
    }

    @Override
    public PaymentResponse processPayment(Long orderId, PaymentRequest paymentRequest) {
        logger.info("Processing payment for order: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getStatus().equals("PENDING_PAYMENT")) {
            throw new BadRequestException("Order is not in pending payment status");
        }

        // Get payment processor
        PaymentProcessor processor = getPaymentProcessor(paymentRequest.getPaymentMethod());

        // Create payment entity
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentRequest.getPaymentMethod());
        payment.setAmount(order.getTotalAmount());
        payment.setStatus("PENDING");

        try {
            // Process payment
            String transactionId = processor.process(order, order.getTotalAmount());

            payment.setTransactionId(transactionId);
            payment.setStatus("SUCCESS");
            payment = paymentRepository.save(payment);

            // Update order status
            order.setStatus("PAID");
            orderRepository.save(order);

            // Reduce stock quantities
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : items) {
                catalogService.updateStock(item.getProduct().getId(), -item.getQuantity());
                logger.info("Stock reduced for product: {}, quantity: {}",
                    item.getProduct().getId(), item.getQuantity());
            }

            // Clear customer cart
            cartService.clearCart(order.getCustomer().getId());

            logger.info("Payment processed successfully: {}", transactionId);

            return mapToPaymentResponse(payment);

        } catch (Exception e) {
            logger.error("Payment failed for order: {}", orderId, e);

            payment.setStatus("FAILED");
            payment.setTransactionId("FAILED");
            paymentRepository.save(payment);

            throw new PaymentProcessingException("Payment processing failed: " + e.getMessage(), e);
        }
    }

    @Override
    public OrderDetailResponse getOrderDetails(Long orderId) {
        logger.info("Fetching order details: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);

        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setSubtotal(order.getSubtotal());
        response.setShippingCost(order.getShippingCost());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setShippingMethod(order.getShippingMethod());
        response.setShippingAddress(order.getShippingAddress());
        response.setBillingAddress(order.getBillingAddress());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemResponse> itemResponses = items.stream()
            .map(this::mapToOrderItemResponse)
            .collect(Collectors.toList());
        response.setItems(itemResponses);

        // Get payment info
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            response.setPaymentStatus(payment.getStatus());
            response.setTransactionId(payment.getTransactionId());
        });

        return response;
    }

    @Override
    public OrderTrackingResponse trackOrder(Long orderId) {
        logger.info("Tracking order: {}", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderTrackingResponse response = new OrderTrackingResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus());
        response.setEstimatedDelivery(calculateEstimatedDelivery(order));
        response.setShippingMethod(order.getShippingMethod());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        return response;
    }

    // Helper methods

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" +
            (int)(Math.random() * 1000);
    }

    private BigDecimal calculateShipping(String shippingMethod) {
        return switch (shippingMethod.toUpperCase()) {
            case "STANDARD" -> new BigDecimal("5.00");
            case "EXPRESS" -> new BigDecimal("15.00");
            case "OVERNIGHT" -> new BigDecimal("30.00");
            default -> new BigDecimal("5.00");
        };
    }

    private LocalDateTime calculateEstimatedDelivery(Order order) {
        int daysToAdd = switch (order.getShippingMethod().toUpperCase()) {
            case "STANDARD" -> 7;
            case "EXPRESS" -> 3;
            case "OVERNIGHT" -> 1;
            default -> 7;
        };
        return order.getCreatedAt().plusDays(daysToAdd);
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        // Define valid transitions
        boolean isValid = switch (currentStatus) {
            case "PENDING_PAYMENT" -> newStatus.equals("PAID") || newStatus.equals("CANCELLED");
            case "PAID" -> newStatus.equals("PROCESSING") || newStatus.equals("CANCELLED");
            case "PROCESSING" -> newStatus.equals("SHIPPED") || newStatus.equals("CANCELLED");
            case "SHIPPED" -> newStatus.equals("DELIVERED");
            case "DELIVERED", "CANCELLED" -> false;
            default -> false;
        };

        if (!isValid) {
            throw new BadRequestException(
                String.format("Invalid status transition from %s to %s", currentStatus, newStatus)
            );
        }
    }

    private PaymentProcessor getPaymentProcessor(String paymentMethod) {
        return paymentProcessors.stream()
            .filter(processor -> processor.getComponentName().equalsIgnoreCase(paymentMethod))
            .findFirst()
            .orElseThrow(() -> new BadRequestException(
                "Payment processor not found for method: " + paymentMethod
            ));
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerId(order.getCustomer().getId());
        response.setStatus(order.getStatus());
        response.setSubtotal(order.getSubtotal());
        response.setShippingCost(order.getShippingCost());
        response.setTotalAmount(order.getTotalAmount());
        response.setPaymentMethod(order.getPaymentMethod());
        response.setShippingMethod(order.getShippingMethod());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        response.setItemCount(items.stream().mapToInt(OrderItem::getQuantity).sum());

        return response;
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        response.setSubtotal(item.getSubtotal());

        try {
            var product = catalogService.getProductById(item.getProduct().getId());
            response.setProductName(product.getName());
            response.setProductImageUrl(product.getImageUrl());
        } catch (ResourceNotFoundException e) {
            response.setProductName("Product Not Found");
        }

        return response;
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setPaymentMethod(payment.getPaymentMethod());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setTransactionId(payment.getTransactionId());
        response.setCreatedAt(payment.getCreatedAt());

        return response;
    }
}
