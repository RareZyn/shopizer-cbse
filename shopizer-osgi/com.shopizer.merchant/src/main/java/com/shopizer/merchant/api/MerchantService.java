package com.shopizer.merchant.api;

import com.shopizer.merchant.dto.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Merchant Service Interface
 * Provides merchant store management, inventory, and sales reporting
 *
 * Functional Requirements:
 * - FR-015: Merchant Management
 * - FR-016: Store Management
 * - FR-017: Inventory Management
 * - FR-018: Report and Analytics
 */
public interface MerchantService {
// ========== Merchant Management (FR-015) ==========

    /**
     * Register a new merchant account
     * FR-015: Merchant registration
     *
     * @param request Registration details (name, email, password, phone)
     * @return Newly created merchant profile with id
     */
    MerchantProfileResponse registerMerchant(MerchantRegistrationRequest request);

    /**
     * Authenticate a merchant and issue an access token
     * FR-015: Merchant login
     *
     * @param request Login credentials (email, password)
     * @return Auth response containing JWT/token and merchant info
     */
    AuthResponse login(LoginRequest request);



    // ========== Store and Inventory Management (FR-016) ==========

    /**
     * Create a new merchant store
     * FR-015: Store Management
     *
     * @param request Store details
     * @return Created store
     */
    MerchantStoreResponse createStore(MerchantStoreRequest request);

    /**
     * Get store by ID
     * FR-015: Store Management
     *
     * @param storeId Store ID
     * @return Store details
     */
    MerchantStoreResponse getStoreById(Long storeId);

    /**
     * Get store by merchant ID
     * FR-015: Store Management
     *
     * @param merchantId Merchant ID
     * @return Store details
     */
    MerchantStoreResponse getStoreByMerchantId(Long merchantId);

    /**
     * Update store details
     * FR-015: Store Management
     *
     * @param storeId Store ID
     * @param request Updated store details
     * @return Updated store
     */
    MerchantStoreResponse updateStore(Long storeId, MerchantStoreRequest request);

    /**
     * Activate store
     * FR-015: Store Management
     *
     * @param storeId Store ID
     */
    void activateStore(Long storeId);

    /**
     * Deactivate store
     * FR-015: Store Management
     *
     * @param storeId Store ID
     */
    void deactivateStore(Long storeId);

    /**
     * List all stores for a merchant
     * FR-016: Store Management
     *
     * @param merchantId Merchant ID
     * @return List of stores
     */
    List<MerchantStoreResponse> listStores(Long merchantId);

    /**
     * Delete a store
     * FR-016: Store Management
     *
     * @param merchantId Merchant ID
     * @param storeId Store ID
     */
    void deleteStore(Long merchantId, Long storeId);

    // ========== Inventory Management (FR-017) ==========

    /**
     * Get inventory for store
     * FR-017: Inventory Management
     *
     * @param storeId Store ID
     * @return List of inventory items
     */
    List<InventoryItemResponse> getInventory(Long storeId);

    /**
     * Get inventory item details
     * FR-017: Inventory Management
     *
     * @param storeId Store ID
     * @param productId Product ID
     * @return Inventory item details
     */
    InventoryItemResponse getInventoryItem(Long storeId, Long productId);

    /**
     * Update stock quantity
     * FR-017: Inventory Management
     *
     * @param storeId Store ID
     * @param productId Product ID
     * @param quantity New quantity
     * @return Updated inventory item
     */
    InventoryItemResponse updateStock(Long storeId, Long productId, Integer quantity);

    /**
     * Add stock (increase quantity)
     * FR-017: Inventory Management
     *
     * @param storeId Store ID
     * @param productId Product ID
     * @param quantity Quantity to add
     * @return Updated inventory item
     */
    InventoryItemResponse addStock(Long storeId, Long productId, Integer quantity);

    /**
     * Remove stock (decrease quantity)
     * FR-017: Inventory Management
     *
     * @param storeId Store ID
     * @param productId Product ID
     * @param quantity Quantity to remove
     * @return Updated inventory item
     */
    InventoryItemResponse removeStock(Long storeId, Long productId, Integer quantity);

    /**
     * Get low stock items
     * FR-017: Inventory Management
     *
     * @param storeId Store ID
     * @param threshold Stock threshold
     * @return List of low stock items
     */
    List<InventoryItemResponse> getLowStockItems(Long storeId, Integer threshold);

    // ========== Reporting and Analytics (FR-018) ==========

    /**
     * Get sales report for date range
     *
     * @param storeId Store ID
     * @param startDate Start date
     * @param endDate End date
     * @return Sales report
     */
    SalesReportResponse getSalesReport(Long storeId, LocalDate startDate, LocalDate endDate);

    /**
     * Get daily sales summary
     *
     * @param storeId Store ID
     * @param date Date
     * @return Daily sales summary
     */
    DailySalesResponse getDailySales(Long storeId, LocalDate date);

    /**
     * Get monthly sales summary
     *
     * @param storeId Store ID
     * @param year Year
     * @param month Month
     * @return Monthly sales summary
     */
    MonthlySalesResponse getMonthlySales(Long storeId, Integer year, Integer month);

    /**
     * Get top selling products
     *
     * @param storeId Store ID
     * @param startDate Start date
     * @param endDate End date
     * @param limit Number of products to return
     * @return List of top selling products
     */
    List<ProductSalesResponse> getTopSellingProducts(Long storeId, LocalDate startDate, LocalDate endDate, Integer limit);

    /**
     * Get revenue analytics
     *
     * @param storeId Store ID
     * @param startDate Start date
     * @param endDate End date
     * @return Revenue analytics
     */
    RevenueAnalyticsResponse getRevenueAnalytics(Long storeId, LocalDate startDate, LocalDate endDate);

    /**
     * Get revenue by category
     *
     * @param storeId Store ID
     * @param startDate Start date
     * @param endDate End date
     * @return Revenue breakdown by category
     */
    List<CategoryRevenueResponse> getRevenueByCategory(Long storeId, LocalDate startDate, LocalDate endDate);

    /**
     * Get total revenue for store
     *
     * @param storeId Store ID
     * @return Total revenue
     */
    BigDecimal getTotalRevenue(Long storeId);

    /**
     * Get revenue for period
     *
     * @param storeId Store ID
     * @param startDate Start date
     * @param endDate End date
     * @return Revenue for period
     */
    BigDecimal getRevenue(Long storeId, LocalDate startDate, LocalDate endDate);
}
