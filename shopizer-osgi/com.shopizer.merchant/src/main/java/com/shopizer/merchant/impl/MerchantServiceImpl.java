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
import com.shopizer.merchant.repository.ProductRepository;
import com.shopizer.merchant.repository.OrderRepository;
import com.shopizer.merchant.repository.ProductViewRepository;
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
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ProductViewRepository productViewRepository;
    private final CatalogService catalogService;
    public MerchantServiceImpl(MerchantRepository merchantRepository,
                               MerchantStoreRepository merchantStoreRepository,
                               ProductRepository productRepository,
                               OrderRepository orderRepository,
                               ProductViewRepository productViewRepository,
                               CatalogService catalogService,
                               OrderService orderService) {
        this.merchantRepository = merchantRepository;
        this.merchantStoreRepository = merchantStoreRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.productViewRepository = productViewRepository;
        this.catalogService = catalogService;
    }

    // ========== Merchant Management (FR-015) ==========

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

    // ========== Store Management (FR-016) ==========

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

    // ========== Sales Reports (FR-018) ==========

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

    // ========== Inventory Management (FR-017) ==========

    @Override
    public InventoryItemResponse createProduct(Long merchantId, Long storeId, MerchantProductCreateRequest request) {
        logger.info("Creating product for store: {} under merchant: {}", storeId, merchantId);
        
        Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", merchantId.toString()));
        
        MerchantStore store = merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId.toString()));
        
        if (!store.getMerchant().getId().equals(merchantId)) {
            throw new BadRequestException("Store does not belong to this merchant");
        }
        
        Product product = new Product();
        product.setStore(store);
        product.setName(request.getName());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setReorderLevel(request.getLowStockThreshold());
        product.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            product.setCategory(category);
        }
        
        product = productRepository.save(product);
        return mapToInventoryResponse(product);
    }

    @Override
    public List<InventoryItemResponse> getInventory(Long merchantId) {
        logger.info("Getting inventory for merchant: {}", merchantId);
        
        Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new ResourceNotFoundException("Merchant", "id", merchantId.toString()));
        
        List<MerchantStore> stores = merchantStoreRepository.findByMerchantId(merchantId);
        List<Product> products = new ArrayList<>();
        
        for (MerchantStore store : stores) {
            products.addAll(productRepository.findByStoreId(store.getId()));
        }
        
        return products.stream()
            .map(this::mapToInventoryResponse)
            .collect(Collectors.toList());
    }

    @Override
    public List<InventoryItemResponse> getInventoryByStore(Long merchantId, Long storeId) {
        logger.info("Getting inventory for store: {} under merchant: {}", storeId, merchantId);
        
        MerchantStore store = merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId.toString()));
        
        if (!store.getMerchant().getId().equals(merchantId)) {
            throw new BadRequestException("Store does not belong to this merchant");
        }
        
        List<Product> products = productRepository.findByStoreId(storeId);
        return products.stream()
            .map(this::mapToInventoryResponse)
            .collect(Collectors.toList());
    }

    @Override
    public InventoryItemResponse updateProduct(Long merchantId, Long productId, InventoryUpdateRequest request) {
        logger.info("Updating product: {} for merchant: {}", productId, merchantId);
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId.toString()));
        
        // Verify it belongs to the merchant's store (store_id is an eager FK)
        final Long storeId = product.getStore().getId();
        MerchantStore store = merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId.toString()));
        
        if (!store.getMerchant().getId().equals(merchantId)) {
            throw new BadRequestException("Product does not belong to this merchant");
        }
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getLowStockThreshold() != null) {
            product.setReorderLevel(request.getLowStockThreshold());
        }
        if (request.getIsActive() != null) {
            product.setActive(request.getIsActive());
        }
        if (request.getCategoryId() != null) {
            Category category = new Category();
            category.setId(request.getCategoryId());
            product.setCategory(category);
        }
        
        product = productRepository.save(product);
        return mapToInventoryResponse(product);
    }

    @Override
    public void deleteProduct(Long merchantId, Long productId) {
        logger.info("Deleting product: {} for merchant: {}", productId, merchantId);
        
        // Verify product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId.toString()));
        
        // Verify it belongs to the merchant's store (store_id is an eager FK)
        MerchantStore store = merchantStoreRepository.findById(product.getStore().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Store", "id", product.getStore().getId().toString()));
        
        if (!store.getMerchant().getId().equals(merchantId)) {
            throw new BadRequestException("Product does not belong to this merchant");
        }
        
        productRepository.deleteById(productId);
    }

    @Override
    public List<InventoryItemResponse> getLowStockProducts(Long merchantId, Long storeId) {
        logger.info("Getting low stock products for merchant: {}", merchantId);
        
        List<Product> products;
        if (storeId != null) {
            products = productRepository.findLowStockProducts(storeId);
        } else {
            products = productRepository.findLowStockProductsByMerchant(merchantId);
        }
        
        return products.stream()
            .map(this::mapToInventoryResponse)
            .collect(Collectors.toList());
    }

    private InventoryItemResponse mapToInventoryResponse(Product product) {
        InventoryItemResponse response = new InventoryItemResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setSku(product.getSku());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setIsActive(product.getActive());
        response.setLowStockThreshold(product.getReorderLevel());
        response.setIsLowStock(product.getStockQuantity() != null && 
                               product.getReorderLevel() != null &&
                               product.getStockQuantity() <= product.getReorderLevel());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
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

    /**
     * FR-018: Get sales report for merchant
     * Supports optional store and date range filtering
     * Returns aggregated order metrics
     */
    @Override
    public SalesReportResponse getSalesReport(Long merchantId, Long storeId, LocalDate startDate, LocalDate endDate) {
        logger.info("📊 Sales report - merchant: {}, store: {}, dates: {} to {}", 
            merchantId, storeId, startDate, endDate);
        
        // Verify merchant exists
        Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        // Verify store belongs to merchant if provided
        MerchantStore store = null;
        if (storeId != null) {
            store = merchantStoreRepository.findByIdAndMerchantId(storeId, merchantId)
                .orElseThrow(() -> new RuntimeException("Store does not belong to this merchant"));
        }
        
        SalesReportResponse report = new SalesReportResponse();
        report.setStoreId(storeId);
        report.setStoreName(store != null ? store.getStoreName() : "All Stores");
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        
        // Get all orders for store
        List<Order> orders = getOrdersByStoreAndDate(storeId != null ? storeId : (store != null ? store.getId() : null), 
                                                     startDate, endDate);
        
        // Calculate metrics
        int totalOrders = orders.size();
        int completedOrders = (int) orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.DELIVERED || o.getStatus() == Order.OrderStatus.SHIPPED)
            .count();
        int cancelledOrders = (int) orders.stream()
            .filter(o -> o.getStatus() == Order.OrderStatus.CANCELLED)
            .count();
        
        BigDecimal totalRevenue = orders.stream()
            .map(o -> o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgOrderValue = totalOrders > 0 
            ? totalRevenue.divide(new BigDecimal(totalOrders), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        
        report.setTotalOrders(totalOrders);
        report.setCompletedOrders(completedOrders);
        report.setCancelledOrders(cancelledOrders);
        report.setTotalRevenue(totalRevenue);
        report.setAverageOrderValue(avgOrderValue);
        report.setTopProducts(new ArrayList<>());
        
        logger.info("✅ Sales report: {} orders, {} revenue", totalOrders, totalRevenue);
        return report;
    }

    /**
     * FR-018: Get per-product sales report
     * Returns sales metrics for each product with optional filters
     */
    @Override
    public List<ProductReportResponse> getProductReport(Long merchantId, Long storeId, Long categoryId,
                                                         Long productId, LocalDate startDate, LocalDate endDate) {
        logger.info("📈 Product report - merchant: {}, store: {}, category: {}", merchantId, storeId, categoryId);
        
        // Verify merchant exists
        merchantRepository.findById(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        List<ProductReportResponse> reports = new ArrayList<>();
        
        // Get all orders for store
        List<Order> orders = getOrdersByStoreAndDate(storeId, startDate, endDate);
        
        // Group OrderItems by Product
        Map<Product, List<OrderItem>> itemsByProduct = new HashMap<>();
        for (Order order : orders) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    Product p = item.getProduct();
                    
                    // Apply filters
                    if (productId != null && !p.getId().equals(productId)) continue;
                    if (categoryId != null && (p.getCategory() == null || !p.getCategory().getId().equals(categoryId))) continue;
                    
                    itemsByProduct.computeIfAbsent(p, k -> new ArrayList<>()).add(item);
                }
            }
        }
        
        // Calculate metrics for each product
        for (Map.Entry<Product, List<OrderItem>> entry : itemsByProduct.entrySet()) {
            Product product = entry.getKey();
            List<OrderItem> items = entry.getValue();
            
            long unitsSold = items.stream().mapToLong(OrderItem::getQuantity).sum();
            BigDecimal totalRevenue = items.stream()
                .map(oi -> BigDecimal.valueOf(oi.getQuantity()).multiply(oi.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            int orderCount = (int) items.stream().map(OrderItem::getOrder).distinct().count();
            
            ProductReportResponse report = new ProductReportResponse();
            report.setProductId(product.getId());
            report.setProductName(product.getName());
            report.setSku(product.getSku());
            report.setUnitsSold(unitsSold);
            report.setTotalRevenue(totalRevenue);
            report.setOrderCount(orderCount);
            
            reports.add(report);
        }
        
        // Sort by revenue descending
        reports.sort((a, b) -> b.getTotalRevenue().compareTo(a.getTotalRevenue()));
        
        logger.info("✅ Product report: {} products", reports.size());
        return reports;
    }

    /**
     * FR-018: Get detailed product analytics
     * Includes daily breakdown and conversion metrics
     */
    @Override
    public ProductAnalyticsResponse getProductAnalytics(Long merchantId, Long productId, 
                                                         LocalDate startDate, LocalDate endDate) {
        logger.info("📊 Product analytics - merchant: {}, product: {}", merchantId, productId);
        
        // Verify merchant exists
        merchantRepository.findById(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        // Verify product exists and belongs to merchant's store
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        merchantStoreRepository.findByIdAndMerchantId(product.getStore().getId(), merchantId)
            .orElseThrow(() -> new RuntimeException("Product store does not belong to merchant"));
        
        ProductAnalyticsResponse analytics = new ProductAnalyticsResponse();
        analytics.setProductId(productId);
        analytics.setProductName(product.getName());
        analytics.setSku(product.getSku());
        analytics.setStartDate(startDate);
        analytics.setEndDate(endDate);
        
        // Get all orders for this product
        List<Order> orders = getOrdersByStoreAndDate(product.getStore().getId(), startDate, endDate);
        
        long totalUnitsSold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int orderCount = 0;
        
        for (Order order : orders) {
            if (order.getItems() != null) {
                for (OrderItem item : order.getItems()) {
                    if (item.getProduct().getId().equals(productId)) {
                        totalUnitsSold += item.getQuantity();
                        totalRevenue = totalRevenue.add(
                            BigDecimal.valueOf(item.getQuantity()).multiply(item.getPrice())
                        );
                        orderCount++;
                    }
                }
            }
        }
        
        analytics.setTotalUnitsSold(totalUnitsSold);
        analytics.setTotalRevenue(totalRevenue);
        analytics.setAverageUnitPrice(totalUnitsSold > 0 
            ? totalRevenue.divide(new BigDecimal(totalUnitsSold), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO);
        analytics.setTotalOrders(orderCount);
        
        // Get view count for conversion rate calculation
        LocalDateTime startDT = startDate != null ? startDate.atStartOfDay() : LocalDateTime.now().minusMonths(1);
        LocalDateTime endDT = endDate != null ? endDate.atTime(23, 59, 59) : LocalDateTime.now();
        long pageViews = productViewRepository.countByProductIdAndViewedAtBetween(productId, startDT, endDT);
        analytics.setPageViews((int) pageViews);
        
        // Calculate conversion rate (orders / views * 100)
        double conversionRate = pageViews > 0 ? (orderCount * 100.0) / pageViews : 0.0;
        analytics.setConversionRate(BigDecimal.valueOf(conversionRate).setScale(2, java.math.RoundingMode.HALF_UP));
        
        logger.info("✅ Product analytics: {} units, {} revenue, {} views, {}% conversion", 
            totalUnitsSold, totalRevenue, pageViews, conversionRate);
        return analytics;
    }

    /**
     * FR-018: Record product view for conversion tracking
     */
    @Override
    public void recordProductView(Long merchantId, Long productId) {
        logger.info("👁️ Recording product view - merchant: {}, product: {}", merchantId, productId);
        
        // Verify merchant exists
        merchantRepository.findById(merchantId)
            .orElseThrow(() -> new RuntimeException("Merchant not found"));
        
        // Verify product exists
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Verify store belongs to merchant
        MerchantStore store = merchantStoreRepository.findByIdAndMerchantId(product.getStore().getId(), merchantId)
            .orElseThrow(() -> new RuntimeException("Product store does not belong to merchant"));
        
        // Save product view to product_view_events table
        ProductView view = new ProductView();
        view.setProduct(product);
        view.setStore(store);
        view.setViewedAt(LocalDateTime.now());
        productViewRepository.save(view);
        
        logger.info("✅ Product view recorded");
    }

    /**
     * Helper: Get all orders for a store within date range
     */
    private List<Order> getOrdersByStoreAndDate(Long storeId, LocalDate startDate, LocalDate endDate) {
        if (storeId == null) {
            return new ArrayList<>();
        }
        
        List<Order> orders = orderRepository.findByStoreId(storeId);
        
        if (startDate != null && endDate != null) {
            LocalDateTime startDT = startDate.atStartOfDay();
            LocalDateTime endDT = endDate.atTime(23, 59, 59);
            
            orders = orders.stream()
                .filter(o -> o.getCreatedAt() != null && 
                           o.getCreatedAt().isAfter(startDT) && 
                           o.getCreatedAt().isBefore(endDT))
                .collect(Collectors.toList());
        }
        
        return orders;
    }
}
