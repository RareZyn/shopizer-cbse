package com.shopizer.rest.servlet;

import com.shopizer.order.api.OrderService;
import com.shopizer.order.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * REST Controller for Order Service
 *
 * Endpoints:
 * POST   /api/v1/orders                          - Create order
 * GET    /api/v1/orders/{id}                     - Get order by ID
 * GET    /api/v1/orders/history?customerId={id}  - Get order history
 * PUT    /api/v1/orders/{id}/status              - Update order status
 * POST   /api/v1/orders/{id}/cancel              - Cancel order
 * POST   /api/v1/orders/{id}/payment             - Process payment
 * GET    /api/v1/orders/{id}/details             - Get order details
 * GET    /api/v1/orders/{id}/tracking            - Track order
 */
public class OrderServlet extends BaseServlet {

    private OrderService orderService;

    public OrderServlet(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequest(response, "Order ID or customerId is required");
                return;
            }

            if (pathInfo.equals("/history")) {
                // GET /api/v1/orders/history?customerId={id}
                handleGetHistory(request, response);
            } else if (pathInfo.endsWith("/details")) {
                // GET /api/v1/orders/{id}/details
                handleGetDetails(request, response);
            } else if (pathInfo.endsWith("/tracking")) {
                // GET /api/v1/orders/{id}/tracking
                handleGetTracking(request, response);
            } else {
                // GET /api/v1/orders/{id}
                handleGetById(request, response);
            }
        } catch (Exception e) {
            logger.error("Error in GET request", e);
            sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // POST /api/v1/orders
                OrderRequest orderRequest = readJsonBody(request, OrderRequest.class);
                OrderResponse created = orderService.createOrder(orderRequest);
                sendCreated(response, created);
            } else if (pathInfo.contains("/cancel")) {
                // POST /api/v1/orders/{id}/cancel
                handleCancelOrder(request, response);
            } else if (pathInfo.contains("/payment")) {
                // POST /api/v1/orders/{id}/payment
                handleProcessPayment(request, response);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in POST request", e);
            sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.contains("/status")) {
                // PUT /api/v1/orders/{id}/status
                handleUpdateStatus(request, response);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in PUT request", e);
            sendInternalError(response, "Error updating order: " + e.getMessage());
        }
    }

    private void handleGetById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long orderId = extractIdFromPath(request, "/api/v1/orders");

        if (orderId == null) {
            sendBadRequest(response, "Invalid order ID");
            return;
        }

        OrderResponse order = orderService.getOrderById(orderId);
        if (order != null) {
            sendSuccess(response, order);
        } else {
            sendNotFound(response, "Order not found with ID: " + orderId);
        }
    }

    private void handleGetHistory(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long customerId = getQueryParamAsLong(request, "customerId");

        if (customerId == null) {
            sendBadRequest(response, "customerId is required");
            return;
        }

        List<OrderResponse> orderHistory = orderService.getOrderHistory(customerId);
        sendSuccess(response, orderHistory);
    }

    private void handleGetDetails(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Order ID is required");
            return;
        }

        try {
            Long orderId = Long.parseLong(parts[1]);
            OrderDetailResponse details = orderService.getOrderDetails(orderId);
            if (details != null) {
                sendSuccess(response, details);
            } else {
                sendNotFound(response, "Order details not found for ID: " + orderId);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid order ID");
        }
    }

    private void handleGetTracking(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Order ID is required");
            return;
        }

        try {
            Long orderId = Long.parseLong(parts[1]);
            OrderTrackingResponse tracking = orderService.trackOrder(orderId);
            if (tracking != null) {
                sendSuccess(response, tracking);
            } else {
                sendNotFound(response, "Order tracking not found for ID: " + orderId);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid order ID");
        }
    }

    private void handleUpdateStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Order ID is required");
            return;
        }

        try {
            Long orderId = Long.parseLong(parts[1]);
            UpdateStatusRequest statusRequest = readJsonBody(request, UpdateStatusRequest.class);

            if (statusRequest.getStatus() == null || statusRequest.getStatus().isEmpty()) {
                sendBadRequest(response, "Status is required");
                return;
            }

            OrderResponse updated = orderService.updateOrderStatus(orderId, statusRequest.getStatus());
            sendSuccess(response, updated);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid order ID");
        }
    }

    private void handleCancelOrder(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Order ID is required");
            return;
        }

        try {
            Long orderId = Long.parseLong(parts[1]);
            CancelOrderRequest cancelRequest = readJsonBody(request, CancelOrderRequest.class);

            String reason = cancelRequest.getReason() != null ? cancelRequest.getReason() : "No reason provided";
            OrderResponse cancelled = orderService.cancelOrder(orderId, reason);
            sendSuccess(response, cancelled);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid order ID");
        }
    }

    private void handleProcessPayment(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Order ID is required");
            return;
        }

        try {
            Long orderId = Long.parseLong(parts[1]);
            PaymentRequest paymentRequest = readJsonBody(request, PaymentRequest.class);

            PaymentResponse paymentResponse = orderService.processPayment(orderId, paymentRequest);
            sendSuccess(response, paymentResponse);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid order ID");
        }
    }

    // Helper DTOs
    static class UpdateStatusRequest {
        private String status;

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    static class CancelOrderRequest {
        private String reason;

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}
