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
 * Sales Reports Endpoints:
 * GET    /api/v1/merchants/{storeId}/reports/sales?start={date}&end={date} - Get sales report
 * GET    /api/v1/merchants/{storeId}/reports/daily?date={date}             - Get daily sales
 * GET    /api/v1/merchants/{storeId}/reports/monthly?year={y}&month={m}    - Get monthly sales
 * GET    /api/v1/merchants/{storeId}/reports/top-products?start={date}&end={date}&limit={n} - Get top products
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

            if (pathInfo != null && pathInfo.equals("/stores")) {
                // POST /api/v1/merchants/stores
                MerchantStoreRequest storeRequest = readJsonBody(request, MerchantStoreRequest.class);
                MerchantStoreResponse created = merchantService.createStore(storeRequest);
                sendCreated(response, created);
            } else if (pathInfo != null && pathInfo.contains("/activate")) {
                // POST /api/v1/merchants/stores/{id}/activate
                handleActivateStore(request, response);
            } else if (pathInfo != null && pathInfo.contains("/deactivate")) {
                // POST /api/v1/merchants/stores/{id}/deactivate
                handleDeactivateStore(request, response);
            } else if (pathInfo != null && pathInfo.contains("/inventory") && pathInfo.contains("/add")) {
                // POST /api/v1/merchants/{storeId}/inventory/{productId}/add
                handleAddStock(request, response);
            } else if (pathInfo != null && pathInfo.contains("/inventory") && pathInfo.contains("/remove")) {
                // POST /api/v1/merchants/{storeId}/inventory/{productId}/remove
                handleRemoveStock(request, response);
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
            } else if (pathInfo != null && pathInfo.contains("/inventory")) {
                // PUT /api/v1/merchants/{storeId}/inventory/{productId}
                handleUpdateStock(request, response);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in PUT request", e);
            sendInternalError(response, "Error updating resource: " + e.getMessage());
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

    // Inventory Management Handlers
    private void handleInventoryGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Store ID is required");
            return;
        }

        try {
            Long storeId = Long.parseLong(parts[1]);

            if (pathInfo.endsWith("/inventory") || pathInfo.endsWith("/inventory/")) {
                // GET /api/v1/merchants/{storeId}/inventory
                List<InventoryItemResponse> inventory = merchantService.getInventory(storeId);
                sendSuccess(response, inventory);
            } else if (pathInfo.contains("/low-stock")) {
                // GET /api/v1/merchants/{storeId}/inventory/low-stock?threshold={n}
                Integer threshold = getQueryParamAsInt(request, "threshold");
                if (threshold == null) {
                    threshold = 10; // Default threshold
                }
                List<InventoryItemResponse> lowStock = merchantService.getLowStockItems(storeId, threshold);
                sendSuccess(response, lowStock);
            } else {
                // GET /api/v1/merchants/{storeId}/inventory/{productId}
                Long productId = Long.parseLong(parts[3]);
                InventoryItemResponse item = merchantService.getInventoryItem(storeId, productId);
                if (item != null) {
                    sendSuccess(response, item);
                } else {
                    sendNotFound(response, "Inventory item not found");
                }
            }
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid store ID or product ID");
        }
    }

    private void handleUpdateStock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 4) {
            sendBadRequest(response, "Store ID and Product ID are required");
            return;
        }

        try {
            Long storeId = Long.parseLong(parts[1]);
            Long productId = Long.parseLong(parts[3]);
            StockUpdateRequest stockRequest = readJsonBody(request, StockUpdateRequest.class);

            InventoryItemResponse updated = merchantService.updateStock(storeId, productId, stockRequest.getQuantity());
            sendSuccess(response, updated);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid store ID or product ID");
        }
    }

    private void handleAddStock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 4) {
            sendBadRequest(response, "Store ID and Product ID are required");
            return;
        }

        try {
            Long storeId = Long.parseLong(parts[1]);
            Long productId = Long.parseLong(parts[3]);
            StockUpdateRequest stockRequest = readJsonBody(request, StockUpdateRequest.class);

            InventoryItemResponse updated = merchantService.addStock(storeId, productId, stockRequest.getQuantity());
            sendSuccess(response, updated);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid store ID or product ID");
        }
    }

    private void handleRemoveStock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 4) {
            sendBadRequest(response, "Store ID and Product ID are required");
            return;
        }

        try {
            Long storeId = Long.parseLong(parts[1]);
            Long productId = Long.parseLong(parts[3]);
            StockUpdateRequest stockRequest = readJsonBody(request, StockUpdateRequest.class);

            InventoryItemResponse updated = merchantService.removeStock(storeId, productId, stockRequest.getQuantity());
            sendSuccess(response, updated);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid store ID or product ID");
        }
    }

    // Sales Reports Handlers
    private void handleReportsGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 2) {
            sendBadRequest(response, "Store ID is required");
            return;
        }

        try {
            Long storeId = Long.parseLong(parts[1]);

            if (pathInfo.contains("/sales")) {
                // GET /api/v1/merchants/{storeId}/reports/sales?start={date}&end={date}
                String startStr = request.getParameter("start");
                String endStr = request.getParameter("end");

                if (startStr == null || endStr == null) {
                    sendBadRequest(response, "start and end dates are required");
                    return;
                }

                LocalDate start = LocalDate.parse(startStr);
                LocalDate end = LocalDate.parse(endStr);

                SalesReportResponse report = merchantService.getSalesReport(storeId, start, end);
                sendSuccess(response, report);
            } else if (pathInfo.contains("/daily")) {
                // GET /api/v1/merchants/{storeId}/reports/daily?date={date}
                String dateStr = request.getParameter("date");

                if (dateStr == null) {
                    sendBadRequest(response, "date is required");
                    return;
                }

                LocalDate date = LocalDate.parse(dateStr);
                DailySalesResponse daily = merchantService.getDailySales(storeId, date);
                sendSuccess(response, daily);
            } else if (pathInfo.contains("/monthly")) {
                // GET /api/v1/merchants/{storeId}/reports/monthly?year={y}&month={m}
                Integer year = getQueryParamAsInt(request, "year");
                Integer month = getQueryParamAsInt(request, "month");

                if (year == null || month == null) {
                    sendBadRequest(response, "year and month are required");
                    return;
                }

                MonthlySalesResponse monthly = merchantService.getMonthlySales(storeId, year, month);
                sendSuccess(response, monthly);
            } else if (pathInfo.contains("/top-products")) {
                // GET /api/v1/merchants/{storeId}/reports/top-products?start={date}&end={date}&limit={n}
                String startStr = request.getParameter("start");
                String endStr = request.getParameter("end");
                Integer limit = getQueryParamAsInt(request, "limit");

                if (startStr == null || endStr == null) {
                    sendBadRequest(response, "start and end dates are required");
                    return;
                }

                LocalDate start = LocalDate.parse(startStr);
                LocalDate end = LocalDate.parse(endStr);
                if (limit == null) limit = 10;

                List<ProductSalesResponse> topProducts = merchantService.getTopSellingProducts(storeId, start, end, limit);
                sendSuccess(response, topProducts);
            } else {
                sendBadRequest(response, "Invalid report endpoint");
            }
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid store ID");
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
                String startStr = request.getParameter("start");
                String endStr = request.getParameter("end");

                if (startStr == null || endStr == null) {
                    sendBadRequest(response, "start and end dates are required");
                    return;
                }

                LocalDate start = LocalDate.parse(startStr);
                LocalDate end = LocalDate.parse(endStr);

                RevenueAnalyticsResponse analytics = merchantService.getRevenueAnalytics(storeId, start, end);
                sendSuccess(response, analytics);
            } else if (pathInfo.contains("/category-revenue")) {
                // GET /api/v1/merchants/{storeId}/analytics/category-revenue?start={date}&end={date}
                String startStr = request.getParameter("start");
                String endStr = request.getParameter("end");

                if (startStr == null || endStr == null) {
                    sendBadRequest(response, "start and end dates are required");
                    return;
                }

                LocalDate start = LocalDate.parse(startStr);
                LocalDate end = LocalDate.parse(endStr);

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
        } catch (Exception e) {
            logger.error("Error in analytics", e);
            sendInternalError(response, "Error generating analytics: " + e.getMessage());
        }
    }

    // Helper DTOs
    static class MessageResponse {
        private String message;

        public MessageResponse(String message) { this.message = message; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
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
}
