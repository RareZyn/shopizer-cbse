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
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
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

    /**
     * Refactored Constructor: Removed List<PaymentProcessor>.
     * In OSGi, we discover the payment processor dynamically from the registry.
     */
    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderItemRepository orderItemRepository,
                            PaymentRepository paymentRepository,
                            CartService cartService,
                            CatalogService catalogService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentRepository = paymentRepository;
        this.cartService = cartService;
        this.catalogService = catalogService;
    }

    /**
     * Dynamic OSGi Service Discovery (CBSE Requirement)
     * Fetches the appropriate PaymentProcessor component from the OSGi Registry.
     */
    private PaymentProcessor getPaymentProcessorFromRegistry(String methodName) {
        try {
            BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
            
            // Search the registry for all services implementing the PaymentProcessor interface
            ServiceReference<PaymentProcessor>[] refs = 
                (ServiceReference<PaymentProcessor>[]) context.getServiceReferences(
                    PaymentProcessor.class.getName(), null);

            if (refs != null) {
                for (ServiceReference<PaymentProcessor> ref : refs) {
                    PaymentProcessor processor = context.getService(ref);
                    
                    // Match the component name (e.g., "Stripe" or "PayPal")
                    if (processor.getComponentName().equalsIgnoreCase(methodName)) {
                        return processor;
                    }
                    // Release service if it's not the one we need
                    context.ungetService(ref);
                }
            }
        } catch (Exception e) {
            logger.error("CBSE Error: OSGi Service lookup failed: {}", e.getMessage());
        }
        
        throw new BadRequestException("OSGi Component Error: Payment processor not found for method: " + methodName);
    }

    @Override
    public OrderResponse createOrder(OrderRequest request) {
        logger.info("Creating order for customer: {}", request.getCustomerId());

        var cartValidation = cartService.validateCart(request.getCustomerId());
        if (!cartValidation.isValid()) {
            throw new BadRequestException("Cart validation failed: " + String.join(", ", cartValidation.getErrors()));
        }

        BigDecimal cartTotal = cartService.calculateTotal(request.getCustomerId());
        BigDecimal shippingCost = calculateShipping(request.getShippingMethod());
        BigDecimal totalAmount = cartTotal.add(shippingCost);

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        
        Customer customer = new Customer();
        customer.setId(request.getCustomerId());
        order.setCustomer(customer);

        order.setStatus(Order.OrderStatus.PENDING_PAYMENT);
        order.setSubtotal(cartTotal);
        order.setShippingCost(shippingCost);
        order.setTotalAmount(totalAmount);

        // Address mapping logic
        AddressInfo shippingAddress = new AddressInfo();
        shippingAddress.setStreet(request.getShippingAddress().getStreet());
        shippingAddress.setCity(request.getShippingAddress().getCity());
        shippingAddress.setState(request.getShippingAddress().getState());
        shippingAddress.setCountry(request.getShippingAddress().getCountry());
        shippingAddress.setPostalCode(request.getShippingAddress().getPostalCode());
        order.setShippingAddress(shippingAddress);

        if (request.getBillingAddress() != null) {
            AddressInfo billingAddress = new AddressInfo();
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

        order = orderRepository.save(order);

        // Convert Cart Items to Order Items
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
    public PaymentResponse processPayment(Long orderId, PaymentRequest paymentRequest) {
        logger.info("Processing payment for order {} using OSGi dynamic discovery", orderId);

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING_PAYMENT) {
            throw new BadRequestException("Order is not in pending payment status");
        }

        // DYNAMIC CBSE LOOKUP
        PaymentProcessor processor = getPaymentProcessorFromRegistry(paymentRequest.getPaymentMethod());

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(paymentRequest.getPaymentMethod()));
        payment.setAmount(order.getTotalAmount());
        payment.setStatus(Payment.PaymentStatus.PENDING);

        try {
            // Process payment through the discovered component (e.g., Stripe or PayPal)
            String transactionId = processor.process(order, order.getTotalAmount());

            payment.setTransactionId(transactionId);
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment = paymentRepository.save(payment);

            order.setStatus(Order.OrderStatus.PAID);
            orderRepository.save(order);

            // Inventory and Cart cleanup
            List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
            for (OrderItem item : items) {
                catalogService.updateStock(item.getProduct().getId(), -item.getQuantity());
            }
            cartService.clearCart(order.getCustomer().getId());

            logger.info("Payment successful via Component: {}", processor.getComponentName());
            return mapToPaymentResponse(payment);

        } catch (Exception e) {
            logger.error("Payment failed: {}", e.getMessage());
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setTransactionId("FAILED");
            paymentRepository.save(payment);
            throw new PaymentProcessingException("Payment component failure: " + e.getMessage(), e);
        }
    }

    // ========== Remaining Service Methods (Same as original) ==========

    @Override
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToOrderResponse(order);
    }

    @Override
    public List<OrderResponse> getOrderHistory(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(this::mapToOrderResponse).collect(Collectors.toList());
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.setStatus(Order.OrderStatus.valueOf(status));
        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        order.setStatus(Order.OrderStatus.CANCELLED);
        return mapToOrderResponse(orderRepository.save(order));
    }

    @Override
    public OrderDetailResponse getOrderDetails(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        OrderDetailResponse response = new OrderDetailResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus().name());
        return response;
    }

    @Override
    public OrderTrackingResponse trackOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        OrderTrackingResponse response = new OrderTrackingResponse();
        response.setOrderNumber(order.getOrderNumber());
        response.setCurrentStatus(order.getStatus().name());
        return response;
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
    }

    private BigDecimal calculateShipping(String shippingMethod) {
        return switch (shippingMethod.toUpperCase()) {
            case "EXPRESS" -> new BigDecimal("15.00");
            case "OVERNIGHT" -> new BigDecimal("30.00");
            default -> new BigDecimal("5.00");
        };
    }

    private OrderResponse mapToOrderResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setStatus(order.getStatus().name());
        response.setTotal(order.getTotalAmount());
        return response;
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getId());
        response.setOrderId(payment.getOrder().getId());
        response.setStatus(payment.getStatus().name());
        response.setTransactionId(payment.getTransactionId());
        return response;
    }
}