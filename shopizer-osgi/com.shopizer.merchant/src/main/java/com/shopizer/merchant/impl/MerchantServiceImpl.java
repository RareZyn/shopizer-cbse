package com.shopizer.merchant.impl;

import com.shopizer.catalog.api.CatalogService;
import com.shopizer.catalog.dto.ProductResponse;
import com.shopizer.common.entity.*;
import com.shopizer.common.exception.BadRequestException;
import com.shopizer.common.exception.ResourceNotFoundException;
import com.shopizer.merchant.api.MerchantService;
import com.shopizer.merchant.dto.*;
import com.shopizer.merchant.repository.MerchantRepository;
import com.shopizer.merchant.repository.MerchantStoreRepository;
import com.shopizer.order.api.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MerchantServiceImpl implements MerchantService {

    private static final Logger logger = LoggerFactory.getLogger(MerchantServiceImpl.class);

    private final MerchantRepository merchantRepository;
    private final MerchantStoreRepository merchantStoreRepository;
    private final CatalogService catalogService;
    public MerchantServiceImpl(MerchantRepository merchantRepository,
                               MerchantStoreRepository merchantStoreRepository,
                               CatalogService catalogService,
                               OrderService orderService) {
        this.merchantRepository = merchantRepository;
        this.merchantStoreRepository = merchantStoreRepository;
        this.catalogService = catalogService;
    }

    // ========== Store Management (FR-015) ==========

    @Override
    public MerchantProfileResponse registerMerchant(MerchantRegistrationRequest request) {
        logger.info("Registering merchant: {}", request.getEmail());

        if (request.getBusinessName() == null || request.getBusinessName().isBlank()) {
            throw new BadRequestException("Business name is required");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        merchantRepository.findByEmail(request.getEmail())
            .ifPresent(existing -> { throw new BadRequestException("Email already registered"); });

        Merchant merchant = new Merchant();
        merchant.setBusinessName(request.getBusinessName());
        merchant.setEmail(request.getEmail());
        merchant.setPassword(hashPassword(request.getPassword()));
        merchant.setPhone(request.getPhone());
        merchant.setStatus("ACTIVE");

        merchant = merchantRepository.save(merchant);

        MerchantProfileResponse response = new MerchantProfileResponse();
        response.setId(merchant.getId());
        response.setBusinessName(merchant.getBusinessName());
        response.setEmail(merchant.getEmail());
        response.setPhone(merchant.getPhone());
        response.setActive("ACTIVE".equalsIgnoreCase(merchant.getStatus()));
        response.setCreatedAt(merchant.getCreatedAt());

        return response;
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        logger.info("Merchant login: {}", request.getEmail());

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required");
        }

        Merchant merchant = merchantRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("Merchant", "email", request.getEmail()));

        if (!verifyPassword(request.getPassword(), merchant.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        MerchantProfileResponse profile = new MerchantProfileResponse();
        profile.setId(merchant.getId());
        profile.setBusinessName(merchant.getBusinessName());
        profile.setEmail(merchant.getEmail());
        profile.setPhone(merchant.getPhone());
        profile.setActive("ACTIVE".equalsIgnoreCase(merchant.getStatus()));
        profile.setCreatedAt(merchant.getCreatedAt());

        AuthResponse response = new AuthResponse();
        response.setAccessToken("bearer-token-" + System.currentTimeMillis());
        response.setTokenType("Bearer");
        response.setExpiresAt(LocalDateTime.now().plusHours(24));
        response.setMerchant(profile);

        logger.info("Login successful for merchant: {}", merchant.getId());
        return response;
    }

    // ========== Store Management (FR-015) ==========

    @Override
    public MerchantStoreResponse createStore(MerchantStoreRequest request) {
        if (request.getMerchantId() == null) {
            throw new IllegalArgumentException("merchantId is required");
        }
        Merchant merchant = merchantRepository.findById(request.getMerchantId())
                .orElseThrow(() -> new RuntimeException("Merchant not found"));

        MerchantStore store = new MerchantStore();
        store.setMerchant(merchant);
        store.setStoreName(request.getStoreName());
        store.setStoreCode(request.getStoreCode());
        store.setLogoUrl(request.getLogoUrl());
        store.setDescription(request.getDescription());
        store.setCurrency(request.getCurrency());
        store.setDefaultLanguage(request.getDefaultLanguage());
        store.setIsActive(request.getIsActive());
        store.setAddress(request.getAddress());
        // storePhone/email not in entity; add mapping if you keep those columns

        store = merchantStoreRepository.save(store);
        return mapToStoreResponse(store);
    }

    @Override
    public MerchantStoreResponse getStoreById(Long storeId) {
        logger.info("Fetching merchant store: {}", storeId);

        MerchantStore store = merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        return mapToStoreResponse(store);
    }

    @Override
    public MerchantStoreResponse getStoreByMerchantId(Long merchantId) {
        logger.info("Fetching merchant store for merchant: {}", merchantId);

        List<MerchantStore> stores = merchantStoreRepository.findByMerchantId(merchantId);
        if (stores.isEmpty()) {
            throw new ResourceNotFoundException("MerchantStore", "merchantId", merchantId);
        }
        MerchantStore store = stores.get(0);

        return mapToStoreResponse(store);
    }

    @Override
    public MerchantStoreResponse updateStore(Long storeId, MerchantStoreRequest request) {
        logger.info("Updating merchant store: {}", storeId);

        MerchantStore store = merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        if (request.getStoreName() != null) {
            store.setStoreName(request.getStoreName());
        }
        if (request.getStoreCode() != null) {
            store.setStoreCode(request.getStoreCode());
        }
        if (request.getDescription() != null) {
            store.setDescription(request.getDescription());
        // }
        // if (request.getEmail() != null) {
        //     store.setEmail(request.getEmail());
        // }
        // if (request.getStorePhone() != null) {
        //     store.setPhone(request.getStorePhone());
        }
        if (request.getAddress() != null) {
            store.setAddress(request.getAddress());
        }
        if (request.getLogoUrl() != null) {
            store.setLogoUrl(request.getLogoUrl());
        }
        if (request.getCurrency() != null) {
            store.setCurrency(request.getCurrency());
        }
        if (request.getDefaultLanguage() != null) {
            store.setDefaultLanguage(request.getDefaultLanguage());
        }
        if (request.getIsActive() != null) {
            store.setIsActive(request.getIsActive());
        }

        store = merchantStoreRepository.save(store);

        logger.info("Merchant store updated successfully");

        return mapToStoreResponse(store);
    }

    @Override
    public void activateStore(Long storeId) {
        logger.info("Activating merchant store: {}", storeId);

        MerchantStore store = merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        store.setIsActive(true);
        merchantStoreRepository.save(store);

        logger.info("Merchant store activated successfully");
    }

    @Override
    public void deactivateStore(Long storeId) {
        logger.info("Deactivating merchant store: {}", storeId);

        MerchantStore store = merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        store.setIsActive(false);
        merchantStoreRepository.save(store);

        logger.info("Merchant store deactivated successfully");
    }

    @Override
    public List<MerchantStoreResponse> listStores(Long merchantId) {
        logger.info("Listing stores for merchant: {}", merchantId);

        List<MerchantStore> stores = merchantStoreRepository.findByMerchantId(merchantId);
        
        return stores.stream()
                .map(this::mapToStoreResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStore(Long merchantId, Long storeId) {
        logger.info("Deleting store: {} for merchant: {}", storeId, merchantId);

        // Verify the store belongs to the merchant
        MerchantStore store = merchantStoreRepository.findByIdAndMerchantId(storeId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found for merchantId=" + merchantId + ", storeId=" + storeId));

        merchantStoreRepository.delete(store);

        logger.info("Store deleted successfully");
    }

    // ========== Inventory Management (FR-016) ==========

    @Override
    public List<InventoryItemResponse> getInventory(Long storeId) {
        logger.info("Fetching inventory for store: {}", storeId);

        // Verify store exists
        // MerchantStore store = merchantStoreRepository.findById(storeId)
        //     .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        // Get all products for this store
        List<ProductResponse> products = catalogService.getAllProducts();

        // Filter products by store and map to inventory items
        return products.stream()
            .filter(product -> product.getStoreId().equals(storeId))
            .map(this::mapToInventoryItem)
            .collect(Collectors.toList());
    }

    @Override
    public InventoryItemResponse getInventoryItem(Long storeId, Long productId) {
        logger.info("Fetching inventory item for store: {}, product: {}", storeId, productId);

        // Verify store exists
        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        ProductResponse product = catalogService.getProductById(productId);

        if (!product.getStoreId().equals(storeId)) {
            throw new BadRequestException("Product does not belong to this store");
        }

        return mapToInventoryItem(product);
    }

    @Override
    public InventoryItemResponse updateStock(Long storeId, Long productId, Integer quantity) {
        logger.info("Updating stock for store: {}, product: {}, quantity: {}", storeId, productId, quantity);

        // Verify store exists
        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        ProductResponse product = catalogService.getProductById(productId);

        if (!product.getStoreId().equals(storeId)) {
            throw new BadRequestException("Product does not belong to this store");
        }

        // Calculate difference and update
        int currentStock = product.getStockQuantity();
        int difference = quantity - currentStock;

        catalogService.updateStock(productId, difference);

        ProductResponse updatedProduct = catalogService.getProductById(productId);

        logger.info("Stock updated successfully");

        return mapToInventoryItem(updatedProduct);
    }

    @Override
    public InventoryItemResponse addStock(Long storeId, Long productId, Integer quantity) {
        logger.info("Adding stock for store: {}, product: {}, quantity: {}", storeId, productId, quantity);

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        // Verify store exists
        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        ProductResponse product = catalogService.getProductById(productId);

        if (!product.getStoreId().equals(storeId)) {
            throw new BadRequestException("Product does not belong to this store");
        }

        catalogService.updateStock(productId, quantity);

        ProductResponse updatedProduct = catalogService.getProductById(productId);

        logger.info("Stock added successfully");

        return mapToInventoryItem(updatedProduct);
    }

    @Override
    public InventoryItemResponse removeStock(Long storeId, Long productId, Integer quantity) {
        logger.info("Removing stock for store: {}, product: {}, quantity: {}", storeId, productId, quantity);

        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be positive");
        }

        // Verify store exists
        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        ProductResponse product = catalogService.getProductById(productId);

        if (!product.getStoreId().equals(storeId)) {
            throw new BadRequestException("Product does not belong to this store");
        }

        catalogService.updateStock(productId, -quantity);

        ProductResponse updatedProduct = catalogService.getProductById(productId);

        logger.info("Stock removed successfully");

        return mapToInventoryItem(updatedProduct);
    }

    @Override
    public List<InventoryItemResponse> getLowStockItems(Long storeId, Integer threshold) {
        logger.info("Fetching low stock items for store: {}, threshold: {}", storeId, threshold);

        // Verify store exists
        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        List<ProductResponse> products = catalogService.getAllProducts();

        return products.stream()
            .filter(product -> product.getStoreId().equals(storeId))
            .filter(product -> product.getStockQuantity() <= threshold)
            .map(this::mapToInventoryItem)
            .collect(Collectors.toList());
    }

    // ========== Sales Reports (FR-017) ==========

    @Override
    public SalesReportResponse getSalesReport(Long storeId, LocalDate startDate, LocalDate endDate) {
        logger.info("Generating sales report for store: {} from {} to {}", storeId, startDate, endDate);

        MerchantStore store = merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        // TODO: Implement actual order querying
        // For now, return mock data structure
        SalesReportResponse report = new SalesReportResponse();
        report.setStoreId(storeId);
        report.setStoreName(store.getStoreName());
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalOrders(0);
        report.setCompletedOrders(0);
        report.setCancelledOrders(0);
        report.setTotalRevenue(BigDecimal.ZERO);
        report.setAverageOrderValue(BigDecimal.ZERO);
        report.setTopProducts(new ArrayList<>());

        return report;
    }

    @Override
    public DailySalesResponse getDailySales(Long storeId, LocalDate date) {
        logger.info("Fetching daily sales for store: {} on {}", storeId, date);

        // Verify store exists
        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        // TODO: Implement actual daily sales calculation
        DailySalesResponse response = new DailySalesResponse();
        response.setDate(date);
        response.setOrderCount(0);
        response.setTotalRevenue(BigDecimal.ZERO);
        response.setAverageOrderValue(BigDecimal.ZERO);
        response.setItemsSold(0);

        return response;
    }

    @Override
    public MonthlySalesResponse getMonthlySales(Long storeId, Integer year, Integer month) {
        logger.info("Fetching monthly sales for store: {} for {}-{}", storeId, year, month);

        // Verify store exists
        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        // TODO: Implement actual monthly sales calculation
        MonthlySalesResponse response = new MonthlySalesResponse();
        response.setYear(year);
        response.setMonth(month);
        response.setOrderCount(0);
        response.setTotalRevenue(BigDecimal.ZERO);
        response.setAverageOrderValue(BigDecimal.ZERO);
        response.setItemsSold(0);

        return response;
    }

    @Override
    public List<ProductSalesResponse> getTopSellingProducts(Long storeId, LocalDate startDate, LocalDate endDate, Integer limit) {
        logger.info("Fetching top {} selling products for store: {} from {} to {}",
            limit, storeId, startDate, endDate);

        // Verify store exists
        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        // TODO: Implement actual top products calculation
        return new ArrayList<>();
    }

    // ========== Revenue Analytics (FR-018) ==========

    @Override
    public RevenueAnalyticsResponse getRevenueAnalytics(Long storeId, LocalDate startDate, LocalDate endDate) {
        logger.info("Generating revenue analytics for store: {} from {} to {}", storeId, startDate, endDate);

        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        // TODO: Implement actual revenue analytics
        RevenueAnalyticsResponse response = new RevenueAnalyticsResponse();
        response.setStoreId(storeId);
        response.setStartDate(startDate);
        response.setEndDate(endDate);
        response.setTotalRevenue(BigDecimal.ZERO);
        response.setAverageDailyRevenue(BigDecimal.ZERO);
        response.setGrowthRate(BigDecimal.ZERO);
        response.setRevenueByCategory(new ArrayList<>());

        return response;
    }

    @Override
    public List<CategoryRevenueResponse> getRevenueByCategory(Long storeId, LocalDate startDate, LocalDate endDate) {
        logger.info("Generating revenue by category for store: {} from {} to {}", storeId, startDate, endDate);

        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        // TODO: Implement actual revenue by category
        return new ArrayList<>();
    }

    @Override
    public BigDecimal getTotalRevenue(Long storeId) {
        logger.info("Calculating total revenue for store: {}", storeId);

        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        // TODO: Implement actual total revenue calculation
        return BigDecimal.ZERO;
    }

    @Override
    public BigDecimal getRevenue(Long storeId, LocalDate startDate, LocalDate endDate) {
        logger.info("Calculating revenue for store: {} from {} to {}", storeId, startDate, endDate);

        merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        // TODO: Implement actual revenue calculation for period
        return BigDecimal.ZERO;
    }

    // Helper methods

    private MerchantStoreResponse mapToStoreResponse(MerchantStore store) {
        MerchantStoreResponse response = new MerchantStoreResponse();
        response.setId(store.getId());
        response.setMerchantId(store.getMerchant() != null ? store.getMerchant().getId() : null);
        response.setStoreName(store.getStoreName());
            response.setStoreCode(store.getStoreCode());
        response.setDescription(store.getDescription());
            response.setLogoUrl(store.getLogoUrl());
        // response.setStoreEmail(store.getEmail());
        // response.setStorePhone(store.getPhone());
        response.setCurrency(store.getCurrency());
        response.setLanguage(store.getDefaultLanguage());
            response.setIsActive(store.getIsActive());
        response.setCreatedAt(store.getCreatedAt());
        response.setUpdatedAt(store.getUpdatedAt());

        return response;
    }

    private InventoryItemResponse mapToInventoryItem(ProductResponse product) {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setProductId(product.getId());
        response.setProductName(product.getName());
        response.setSku(product.getSku());
        response.setStockQuantity(product.getStockQuantity());
        response.setPrice(product.getPrice());
        response.setCategoryName(product.getCategoryName());
        response.setActive(product.getActive());
        response.setLastUpdated(product.getUpdatedAt());
        return response;
    }

    // Password hashing helpers
    private String hashPassword(String plainPassword) {
        return org.mindrot.jbcrypt.BCrypt.hashpw(plainPassword, org.mindrot.jbcrypt.BCrypt.gensalt(12));
    }

    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return org.mindrot.jbcrypt.BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("Error verifying password", e);
            return false;
        }
    }
}
