package com.shopizer.merchant.impl;

import com.shopizer.catalog.api.CatalogService;
import com.shopizer.catalog.dto.ProductResponse;
import com.shopizer.common.entity.*;
import com.shopizer.common.exception.BadRequestException;
import com.shopizer.common.exception.ResourceNotFoundException;
import com.shopizer.merchant.api.MerchantService;
import com.shopizer.merchant.dto.*;
import com.shopizer.merchant.repository.MerchantStoreRepository;
import com.shopizer.order.api.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MerchantServiceImpl implements MerchantService {

    private static final Logger logger = LoggerFactory.getLogger(MerchantServiceImpl.class);

    private final MerchantStoreRepository merchantStoreRepository;
    private final CatalogService catalogService;
    private final OrderService orderService;

    public MerchantServiceImpl(MerchantStoreRepository merchantStoreRepository,
                              CatalogService catalogService,
                              OrderService orderService) {
        this.merchantStoreRepository = merchantStoreRepository;
        this.catalogService = catalogService;
        this.orderService = orderService;
    }

    // ========== Store Management (FR-015) ==========

    @Override
    public MerchantStoreResponse createStore(MerchantStoreRequest request) {
        logger.info("Creating merchant store: {}", request.getStoreName());

        // Check if merchant already has a store
        Optional<MerchantStore> existingStore = merchantStoreRepository.findByMerchantId(request.getMerchantId());
        if (existingStore.isPresent()) {
            throw new BadRequestException("Merchant already has a store");
        }

        MerchantStore store = new MerchantStore();
        Merchant merchant = new Merchant();
        merchant.setId(request.getMerchantId());
        store.setMerchant(merchant);

        store.setStoreName(request.getStoreName());
        store.setDescription(request.getDescription());
        store.setStoreEmail(request.getStoreEmail());
        store.setStorePhone(request.getStorePhone());
        store.setCurrency(request.getCurrency() != null ? request.getCurrency() : "USD");
        store.setLanguage(request.getLanguage() != null ? request.getLanguage() : "en");
        store.setActive(true);

        Address address = new Address();
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setCountry(request.getCountry());
        address.setPostalCode(request.getPostalCode());
        store.setAddress(address);

        store = merchantStoreRepository.save(store);

        logger.info("Merchant store created successfully: {}", store.getStoreName());

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

        MerchantStore store = merchantStoreRepository.findByMerchantId(merchantId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "merchantId", merchantId));

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
        if (request.getDescription() != null) {
            store.setDescription(request.getDescription());
        }
        if (request.getStoreEmail() != null) {
            store.setStoreEmail(request.getStoreEmail());
        }
        if (request.getStorePhone() != null) {
            store.setStorePhone(request.getStorePhone());
        }
        if (request.getCurrency() != null) {
            store.setCurrency(request.getCurrency());
        }
        if (request.getLanguage() != null) {
            store.setLanguage(request.getLanguage());
        }

        // Update address if provided
        if (request.getStreet() != null || request.getCity() != null) {
            Address address = store.getAddress();
            if (address == null) {
                address = new Address();
                store.setAddress(address);
            }
            if (request.getStreet() != null) address.setStreet(request.getStreet());
            if (request.getCity() != null) address.setCity(request.getCity());
            if (request.getState() != null) address.setState(request.getState());
            if (request.getCountry() != null) address.setCountry(request.getCountry());
            if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
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

        store.setActive(true);
        merchantStoreRepository.save(store);

        logger.info("Merchant store activated successfully");
    }

    @Override
    public void deactivateStore(Long storeId) {
        logger.info("Deactivating merchant store: {}", storeId);

        MerchantStore store = merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        store.setActive(false);
        merchantStoreRepository.save(store);

        logger.info("Merchant store deactivated successfully");
    }

    // ========== Inventory Management (FR-016) ==========

    @Override
    public List<InventoryItemResponse> getInventory(Long storeId) {
        logger.info("Fetching inventory for store: {}", storeId);

        // Verify store exists
        MerchantStore store = merchantStoreRepository.findById(storeId)
            .orElseThrow(() -> new ResourceNotFoundException("MerchantStore", "id", storeId));

        // Get all products for this store
        List<ProductResponse> products = catalogService.getAllProducts();

        // Filter products by store and map to inventory items
        return products.stream()
            .filter(product -> product.getMerchantStoreId().equals(storeId))
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

        if (!product.getMerchantStoreId().equals(storeId)) {
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

        if (!product.getMerchantStoreId().equals(storeId)) {
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

        if (!product.getMerchantStoreId().equals(storeId)) {
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

        if (!product.getMerchantStoreId().equals(storeId)) {
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
            .filter(product -> product.getMerchantStoreId().equals(storeId))
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
        response.setMerchantId(store.getMerchant().getId());
        response.setStoreName(store.getStoreName());
        response.setDescription(store.getDescription());
        response.setStoreEmail(store.getStoreEmail());
        response.setStorePhone(store.getStorePhone());
        response.setCurrency(store.getCurrency());
        response.setLanguage(store.getLanguage());
        response.setActive(store.getActive());
        response.setCreatedAt(store.getCreatedAt());
        response.setUpdatedAt(store.getUpdatedAt());

        if (store.getAddress() != null) {
            Address address = store.getAddress();
            response.setStreet(address.getStreet());
            response.setCity(address.getCity());
            response.setState(address.getState());
            response.setCountry(address.getCountry());
            response.setPostalCode(address.getPostalCode());
        }

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
}
