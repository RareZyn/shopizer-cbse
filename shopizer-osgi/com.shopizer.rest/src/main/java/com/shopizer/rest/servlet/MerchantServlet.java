package com.shopizer.rest.servlet;

import com.shopizer.merchant.api.MerchantService;
import com.shopizer.merchant.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller for Merchant Service
 *
 * Authentication Endpoints (FR-015):
 * POST   /api/v1/merchants/register    - Register new merchant account
 * POST   /api/v1/merchants/login       - Authenticate merchant (returns JWT token)
 *
 * Store Management Endpoints:
 * POST   /api/v1/merchants/stores                   - Create store
 * GET    /api/v1/merchants/stores/{id}              - Get store by ID
 * GET    /api/v1/merchants?merchantId={id}          - Get store by merchant ID
 * PUT    /api/v1/merchants/stores/{id}              - Update store
 * POST   /api/v1/merchants/stores/{id}/activate     - Activate store
 * POST   /api/v1/merchants/stores/{id}/deactivate   - Deactivate store
 *
 * Inventory Management Endpoints:
 * GET    /api/v1/merchants/{storeId}/inventory              - Get inventory
 * GET    /api/v1/merchants/{storeId}/inventory/{productId}  - Get inventory item
 * PUT    /api/v1/merchants/{storeId}/inventory/{productId}  - Update stock
 * POST   /api/v1/merchants/{storeId}/inventory/{productId}/add    - Add stock
 * POST   /api/v1/merchants/{storeId}/inventory/{productId}/remove - Remove stock
 * GET    /api/v1/merchants/{storeId}/inventory/low-stock?threshold={n} - Get low stock items
 *
 * Sales Reports Endpoints (FR-018):
 * GET    /api/v1/merchants/{merchantId}/reports/sales?storeId=&startDate=&endDate= - Sales report (store optional)
 * GET    /api/v1/merchants/{merchantId}/reports/products?storeId=&categoryId=&productId=&startDate=&endDate= - Product report
 * GET    /api/v1/merchants/{merchantId}/reports/products/{productId}/analytics?startDate=&endDate= - Product analytics
 * POST   /api/v1/merchants/{merchantId}/products/{productId}/views - Record product view (conversion tracking)
 *
 * Revenue Analytics Endpoints:
 * GET    /api/v1/merchants/{storeId}/analytics/revenue?start={date}&end={date} - Get revenue analytics
 * GET    /api/v1/merchants/{storeId}/analytics/category-revenue?start={date}&end={date} - Get category revenue
 * GET    /api/v1/merchants/{storeId}/analytics/total-revenue - Get total revenue
 */
public class MerchantServlet extends BaseServlet {

    private MerchantService merchantService;

    public MerchantServlet(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/merchants?merchantId={id}
                handleGetStoreByMerchantId(request, response);
            } else if (pathInfo.matches("/\\d+/stores")) {
                // GET /api/v1/merchants/{merchantId}/stores - List all stores for merchant
                handleListStoresByMerchant(request, response);
            } else if (pathInfo.startsWith("/stores/")) {
                // GET /api/v1/merchants/stores/{id}
                handleGetStoreById(request, response);
            } else if (pathInfo.contains("/inventory")) {
                handleInventoryGet(request, response);
            } else if (pathInfo.contains("/reports")) {
                handleReportsGet(request, response);
            } else if (pathInfo.contains("/analytics")) {
                handleAnalyticsGet(request, response);
            } else {
                sendBadRequest(response, "Invalid endpoint");
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

            if (pathInfo != null && pathInfo.equals("/register")) {
                // POST /api/v1/merchants/register (FR-015)
                handleMerchantRegister(request, response);
            } else if (pathInfo != null && pathInfo.equals("/login")) {
                // POST /api/v1/merchants/login (FR-015)
                handleMerchantLogin(request, response);
            } else if (pathInfo != null && (pathInfo.equals("/stores") || pathInfo.matches("/\\d+/stores"))) {
                // POST /api/v1/merchants/stores OR /api/v1/merchants/{merchantId}/stores
                MerchantStoreRequest storeRequest = readJsonBody(request, MerchantStoreRequest.class);
                
                // Extract merchantId from URL if present: /merchants/{merchantId}/stores
                if (pathInfo.matches("/\\d+/stores")) {
                    String[] parts = pathInfo.split("/");
                    Long merchantId = Long.parseLong(parts[1]);
                    storeRequest.setMerchantId(merchantId);
                }
                
                // Validate merchantId is set
                if (storeRequest.getMerchantId() == null) {
                    sendBadRequest(response, "merchantId is required (in URL path or request body)");
                    return;
                }
                
                MerchantStoreResponse created = merchantService.createStore(storeRequest);
                sendCreated(response, created);
            } else if (pathInfo != null && pathInfo.contains("/activate")) {
                // POST /api/v1/merchants/stores/{id}/activate
                handleActivateStore(request, response);
            } else if (pathInfo != null && pathInfo.contains("/deactivate")) {
                // POST /api/v1/merchants/stores/{id}/deactivate
                handleDeactivateStore(request, response);
            } else if (pathInfo != null && pathInfo.matches("/\\d+/stores/\\d+/products")) {
                // POST /api/v1/merchants/{merchantId}/stores/{storeId}/products (FR-017)
                handleCreateProduct(request, response);
            } else if (pathInfo != null && pathInfo.matches("/\\d+/products/\\d+/views")) {
                // POST /api/v1/merchants/{merchantId}/products/{productId}/views (FR-018)
                handleRecordProductView(request, response);
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

            if (pathInfo != null && pathInfo.startsWith("/stores/") && !pathInfo.contains("/inventory")) {
                // PUT /api/v1/merchants/stores/{id}
                handleUpdateStore(request, response);
            } else if (pathInfo != null && pathInfo.matches("/\\d+/inventory/products/\\d+")) {
                // PUT /api/v1/merchants/{merchantId}/inventory/products/{productId} (FR-017)
                handleUpdateProduct(request, response);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in PUT request", e);
            sendInternalError(response, "Error updating resource: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.matches("/\\d+/stores/\\d+")) {
                // DELETE /api/v1/merchants/{merchantId}/stores/{storeId}
                handleDeleteStore(request, response);
            } else if (pathInfo != null && pathInfo.matches("/\\d+/inventory/products/\\d+")) {
                // DELETE /api/v1/merchants/{merchantId}/inventory/products/{productId} (FR-017)
                handleDeleteProduct(request, response);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in DELETE request", e);
            sendInternalError(response, "Error deleting resource: " + e.getMessage());
        }
    }

    // Store Management Handlers
    private void handleGetStoreById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 3) {
            sendBadRequest(response, "Store ID is required");
            return;
        }

        try {
            Long storeId = Long.parseLong(parts[2]);
            MerchantStoreResponse store = merchantService.getStoreById(storeId);
            if (store != null) {
                sendSuccess(response, store);
            } else {
                sendNotFound(response, "Store not found with ID: " + storeId);
            }
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid store ID");
        }
    }

    private void handleGetStoreByMerchantId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long merchantId = getQueryParamAsLong(request, "merchantId");

        if (merchantId == null) {
            sendBadRequest(response, "merchantId is required");
            return;
        }

        MerchantStoreResponse store = merchantService.getStoreByMerchantId(merchantId);
        if (store != null) {
            sendSuccess(response, store);
        } else {
            sendNotFound(response, "Store not found for merchant ID: " + merchantId);
        }
    }

    private void handleListStoresByMerchant(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Merchant ID is required");
            return;
        }

        try {
            Long merchantId = Long.parseLong(parts[1]);
            List<MerchantStoreResponse> stores = merchantService.listStores(merchantId);
            sendSuccess(response, stores);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid merchant ID");
        }
    }

    private void handleDeleteStore(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 4) {
            sendBadRequest(response, "Merchant ID and Store ID are required");
            return;
        }

        try {
            Long merchantId = Long.parseLong(parts[1]);
            Long storeId = Long.parseLong(parts[3]);
            merchantService.deleteStore(merchantId, storeId);
            sendSuccess(response, new MessageResponse("Store deleted successfully"));
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid merchant ID or store ID");
        }
    }

    private void handleUpdateStore(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 3) {
            sendBadRequest(response, "Store ID is required");
            return;
        }

        try {
            Long storeId = Long.parseLong(parts[2]);
            MerchantStoreRequest storeRequest = readJsonBody(request, MerchantStoreRequest.class);
            MerchantStoreResponse updated = merchantService.updateStore(storeId, storeRequest);
            sendSuccess(response, updated);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid store ID");
        }
    }

    private void handleActivateStore(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 3) {
            sendBadRequest(response, "Store ID is required");
            return;
        }

        try {
            Long storeId = Long.parseLong(parts[2]);
            merchantService.activateStore(storeId);
            sendSuccess(response, new MessageResponse("Store activated successfully"));
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid store ID");
        }
    }

    private void handleDeactivateStore(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 3) {
            sendBadRequest(response, "Store ID is required");
            return;
        }

        try {
            Long storeId = Long.parseLong(parts[2]);
            merchantService.deactivateStore(storeId);
            sendSuccess(response, new MessageResponse("Store deactivated successfully"));
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid store ID");
        }
    }

    // Sales Reports Handlers
    private void handleReportsGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Merchant ID is required");
            return;
        }

        try {
            Long merchantId = Long.parseLong(parts[1]);

            if (pathInfo.contains("/reports/sales")) {
                // GET /api/v1/merchants/{merchantId}/reports/sales?storeId=&startDate=&endDate=
                LocalDate start = parseDateParam(request, "startDate", "start");
                LocalDate end = parseDateParam(request, "endDate", "end");
                if (start == null || end == null) {
                    sendBadRequest(response, "startDate and endDate are required");
                    return;
                }

                Long storeId = getQueryParamAsLong(request, "storeId");
                SalesReportResponse report = merchantService.getSalesReport(merchantId, storeId, start, end);
                sendSuccess(response, report);

            } else if (pathInfo.matches("/\\d+/reports/products/\\d+/analytics")) {
                // GET /api/v1/merchants/{merchantId}/reports/products/{productId}/analytics?startDate=&endDate=
                Long productId = Long.parseLong(parts[4]);
                LocalDate start = parseDateParam(request, "startDate", "start");
                LocalDate end = parseDateParam(request, "endDate", "end");
                ProductAnalyticsResponse analytics = merchantService.getProductAnalytics(merchantId, productId, start, end);
                sendSuccess(response, analytics);

            } else if (pathInfo.contains("/reports/products")) {
                // GET /api/v1/merchants/{merchantId}/reports/products?storeId=&categoryId=&productId=&startDate=&endDate=
                Long storeId = getQueryParamAsLong(request, "storeId");
                Long categoryId = getQueryParamAsLong(request, "categoryId");
                Long productId = getQueryParamAsLong(request, "productId");
                LocalDate start = parseDateParam(request, "startDate", "start");
                LocalDate end = parseDateParam(request, "endDate", "end");

                List<ProductReportResponse> products = merchantService.getProductReport(merchantId, storeId, categoryId, productId, start, end);
                sendSuccess(response, products);
            } else {
                sendBadRequest(response, "Invalid report endpoint");
            }
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid merchant or product ID");
        } catch (IllegalArgumentException e) {
            sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            logger.error("Error in reports", e);
            sendInternalError(response, "Error generating report: " + e.getMessage());
        }
    }

    // Revenue Analytics Handlers
    private void handleAnalyticsGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Store ID is required");
            return;
        }

        try {
            Long storeId = Long.parseLong(parts[1]);

            if (pathInfo.contains("/revenue") && !pathInfo.contains("/category")) {
                // GET /api/v1/merchants/{storeId}/analytics/revenue?start={date}&end={date}
                LocalDate start = parseDateParam(request, "start", null);
                LocalDate end = parseDateParam(request, "end", null);
                if (start == null || end == null) {
                    sendBadRequest(response, "start and end dates are required");
                    return;
                }

                RevenueAnalyticsResponse analytics = merchantService.getRevenueAnalytics(storeId, start, end);
                sendSuccess(response, analytics);
            } else if (pathInfo.contains("/category-revenue")) {
                // GET /api/v1/merchants/{storeId}/analytics/category-revenue?start={date}&end={date}
                LocalDate start = parseDateParam(request, "start", null);
                LocalDate end = parseDateParam(request, "end", null);
                if (start == null || end == null) {
                    sendBadRequest(response, "start and end dates are required");
                    return;
                }

                List<CategoryRevenueResponse> categoryRevenue = merchantService.getRevenueByCategory(storeId, start, end);
                sendSuccess(response, categoryRevenue);
            } else if (pathInfo.contains("/total-revenue")) {
                // GET /api/v1/merchants/{storeId}/analytics/total-revenue
                BigDecimal totalRevenue = merchantService.getTotalRevenue(storeId);
                sendSuccess(response, new TotalRevenueResponse(totalRevenue));
            } else {
                sendBadRequest(response, "Invalid analytics endpoint");
            }
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid store ID");
        } catch (IllegalArgumentException e) {
            sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            logger.error("Error in analytics", e);
            sendInternalError(response, "Error generating analytics: " + e.getMessage());
        }
    }

    // ========== FR-015: Authentication Handlers ==========

    /**
     * Handle merchant registration (FR-015)
     * POST /api/v1/merchants/register
     * 
     * Request Body:
     * {
     *   "businessName": "Tech Store LLC",
     *   "businessRegistrationNumber": "BRN-2025-001",
     *   "email": "owner@techstore.com",
     *   "password": "SecurePass123",
     *   "phone": "+1-555-0100"
     * }
     */
    private void handleMerchantRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            MerchantRegistrationRequest regRequest = readJsonBody(request, MerchantRegistrationRequest.class);
            
            if (regRequest == null) {
                sendBadRequest(response, "Request body is required");
                return;
            }

            logger.info("Processing merchant registration for: {}", regRequest.getEmail());
            
            MerchantProfileResponse profile = merchantService.registerMerchant(regRequest);
            
            logger.info("Merchant registered successfully: {}", profile.getEmail());
            sendCreated(response, profile);
            
        } catch (com.shopizer.common.exception.BadRequestException e) {
            logger.warn("Registration failed: {}", e.getMessage());
            sendBadRequest(response, e.getMessage());
        } catch (Exception e) {
            logger.error("Registration error", e);
            sendInternalError(response, "Registration failed: " + e.getMessage());
        }
    }

    /**
     * Handle merchant login (FR-015)
     * POST /api/v1/merchants/login
     * 
     * Request Body:
     * {
     *   "email": "owner@techstore.com",
     *   "password": "SecurePass123"
     * }
     * 
     * Response:
     * {
     *   "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "tokenType": "Bearer",
     *   "expiresAt": "2026-01-21T22:30:00",
     *   "merchant": { ... }
     * }
     */
    private void handleMerchantLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            LoginRequest loginRequest = readJsonBody(request, LoginRequest.class);
            
            if (loginRequest == null) {
                sendBadRequest(response, "Request body is required");
                return;
            }

            logger.info("Processing login for: {}", loginRequest.getEmail());
            
            AuthResponse authResponse = merchantService.login(loginRequest);
            
            logger.info("Login successful for: {}", loginRequest.getEmail());
            sendSuccess(response, authResponse);
            
        } catch (com.shopizer.common.exception.BadRequestException e) {
            logger.warn("Login failed: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            sendJsonResponse(response, new ErrorResponse(e.getMessage()), HttpServletResponse.SC_UNAUTHORIZED);
        } catch (Exception e) {
            logger.error("Login error", e);
            sendInternalError(response, "Login failed: " + e.getMessage());
        }
    }

    // Helper DTOs
    static class ErrorResponse {
        private String error;
        private long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }

    static class MessageResponse {
        private String message;

        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // Utility: parse date parameter with optional fallback key
    private LocalDate parseDateParam(HttpServletRequest request, String primaryKey, String fallbackKey) {
        String value = request.getParameter(primaryKey);
        if (value == null && fallbackKey != null) {
            value = request.getParameter(fallbackKey);
        }
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Use YYYY-MM-DD");
        }
    }

    static class StockUpdateRequest {
        private Integer quantity;

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    static class TotalRevenueResponse {
        private BigDecimal totalRevenue;

        public TotalRevenueResponse(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }
    }

    // ========== FR-017 Inventory Management Handlers ==========

    private void handleCreateProduct(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        // Path: /merchants/{merchantId}/stores/{storeId}/products
        if (parts.length < 4) {
            sendBadRequest(response, "Merchant ID and Store ID are required");
            return;
        }

        try {
            Long merchantId = Long.parseLong(parts[1]);
            Long storeId = Long.parseLong(parts[3]);
            MerchantProductCreateRequest productRequest = readJsonBody(request, MerchantProductCreateRequest.class);

            InventoryItemResponse created = merchantService.createProduct(merchantId, storeId, productRequest);
            sendCreated(response, created);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid merchant ID or store ID");
        }
    }

    private void handleInventoryGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Merchant ID is required");
            return;
        }

        try {
            Long merchantId = Long.parseLong(parts[1]);

            // GET /api/v1/merchants/{merchantId}/inventory/low-stock
            if (pathInfo.contains("/low-stock")) {
                String storeIdParam = request.getParameter("storeId");
                Long storeId = storeIdParam != null ? Long.parseLong(storeIdParam) : null;
                List<InventoryItemResponse> lowStock = merchantService.getLowStockProducts(merchantId, storeId);
                sendSuccess(response, lowStock);
                return;
            }

            // GET /api/v1/merchants/{merchantId}/stores/{storeId}/inventory
            if (parts.length >= 4 && parts[3].equals("inventory")) {
                Long storeId = Long.parseLong(parts[2]);
                List<InventoryItemResponse> inventory = merchantService.getInventoryByStore(merchantId, storeId);
                sendSuccess(response, inventory);
                return;
            }

            // GET /api/v1/merchants/{merchantId}/inventory
            if (pathInfo.endsWith("/inventory")) {
                List<InventoryItemResponse> inventory = merchantService.getInventory(merchantId);
                sendSuccess(response, inventory);
                return;
            }

            sendBadRequest(response, "Invalid inventory endpoint");
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid merchant ID or store ID");
        }
    }

    private void handleUpdateProduct(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        // Path: /merchants/{merchantId}/inventory/products/{productId}
        if (parts.length < 4) {
            sendBadRequest(response, "Merchant ID and Product ID are required");
            return;
        }

        try {
            Long merchantId = Long.parseLong(parts[1]);
            Long productId = Long.parseLong(parts[4]);
            InventoryUpdateRequest updateRequest = readJsonBody(request, InventoryUpdateRequest.class);

            InventoryItemResponse updated = merchantService.updateProduct(merchantId, productId, updateRequest);
            sendSuccess(response, updated);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            sendBadRequest(response, "Invalid merchant ID or product ID");
        }
    }

    private void handleDeleteProduct(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        // Path: /merchants/{merchantId}/inventory/products/{productId}
        if (parts.length < 4) {
            sendBadRequest(response, "Merchant ID and Product ID are required");
            return;
        }

        try {
            Long merchantId = Long.parseLong(parts[1]);
            Long productId = Long.parseLong(parts[4]);

            merchantService.deleteProduct(merchantId, productId);
            sendSuccess(response, new MessageResponse("Product deleted successfully"));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            sendBadRequest(response, "Invalid merchant ID or product ID");
        }
    }

    // FR-018: Record product view for conversion tracking
    private void handleRecordProductView(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        // Path: /merchants/{merchantId}/products/{productId}/views
        if (parts.length < 5) {
            sendBadRequest(response, "Merchant ID and Product ID are required");
            return;
        }

        try {
            Long merchantId = Long.parseLong(parts[1]);
            Long productId = Long.parseLong(parts[3]);
            merchantService.recordProductView(merchantId, productId);
            sendSuccess(response, new MessageResponse("Product view recorded"));
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid merchant ID or product ID");
        }
    }
}
