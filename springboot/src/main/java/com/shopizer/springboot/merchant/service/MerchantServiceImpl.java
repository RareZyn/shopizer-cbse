package com.shopizer.springboot.merchant.service;

import com.shopizer.springboot.merchant.entity.Merchant;
import com.shopizer.springboot.merchant.entity.MerchantStore;
import com.shopizer.springboot.merchant.repository.MerchantReportRepository;
import com.shopizer.springboot.merchant.repository.MerchantRepository;
import com.shopizer.springboot.merchant.repository.MerchantStoreRepository;
import org.springframework.stereotype.Service;

import com.shopizer.springboot.catalog.entity.Product;
import com.shopizer.springboot.catalog.repository.ProductRepository;

import com.shopizer.springboot.merchant.dto.InventoryItemResponse;
import com.shopizer.springboot.merchant.dto.InventoryUpdateRequest;

import com.shopizer.springboot.merchant.dto.MerchantProductCreateRequest;

import com.shopizer.springboot.merchant.dto.MerchantStoreRequest;
import com.shopizer.springboot.merchant.dto.MerchantStoreResponse;

import com.shopizer.springboot.merchant.dto.SalesReportResponse;
import com.shopizer.springboot.merchant.exception.DuplicateResourceException;
import com.shopizer.springboot.merchant.exception.ResourceNotFoundException;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

/**
 * Merchant Service Implementation
 * FR-015 to FR-018: Merchant functionality
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantStoreRepository storeRepository;
    private final ProductRepository productRepository;// inject Aina's ProductRepository
    private final MerchantReportRepository reportRepository; 



    public MerchantServiceImpl(MerchantRepository merchantRepository, MerchantStoreRepository storeRepository, ProductRepository productRepository, MerchantReportRepository reportRepository) {
        this.merchantRepository = merchantRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.reportRepository = reportRepository;
    }

    @Override
    public Merchant createMerchant(Merchant merchant) {
        return merchantRepository.save(merchant);
    }

    @Override
    public Optional<Merchant> getMerchantById(Long id) {
        return merchantRepository.findById(id);
    }

    @Override
    public List<Merchant> getAllMerchants() {
        return merchantRepository.findAll();
    }

    @Override
    public Merchant updateMerchant(Long id, Merchant merchant) {
        merchant.setId(id);
        return merchantRepository.save(merchant);
    }

    @Override
    public void deleteMerchant(Long id) {
        merchantRepository.deleteById(id);
    }

     @Override
    @Transactional(readOnly = true)
    public List<MerchantStoreResponse> listStores(Long merchantId) {
        // If merchant doesn't exist, return 404
        ensureMerchantExists(merchantId);

        return storeRepository.findByMerchantId(merchantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteStore(Long merchantId, Long storeId) {
        ensureMerchantExists(merchantId);

         MerchantStore store = storeRepository.findByIdAndMerchantId(storeId, merchantId)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "Store not found for merchantId=" + merchantId + ", storeId=" + storeId
            ));

    storeRepository.delete(store);
}

    @Override
    @Transactional
    public MerchantStoreResponse createStore(Long merchantId, MerchantStoreRequest req) {
        Merchant merchant = merchantRepository.findById(merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + merchantId));

        // Duplicate store_code (business registration number)
        if (storeRepository.existsByStoreCode(req.storeCode())) {
            throw new DuplicateResourceException("The store already exists (storeCode=" + req.storeCode() + ")");
        }

        MerchantStore store = new MerchantStore();
        store.setMerchant(merchant);
        store.setStoreName(req.storeName());
        store.setStoreCode(req.storeCode());
        store.setAddress(req.address());
        store.setDescription(req.description());
        store.setLogoUrl(req.logoUrl());
        store.setCurrency(req.currency());
        store.setDefaultLanguage(req.defaultLanguage());
        store.setIsActive(req.isActive() != null ? req.isActive() : true);

        MerchantStore saved = storeRepository.save(store);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MerchantStoreResponse getStore(Long merchantId, Long storeId) {
        ensureMerchantExists(merchantId);

        MerchantStore store = storeRepository.findByIdAndMerchantId(storeId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found for merchantId=" + merchantId + ", storeId=" + storeId
                ));

        return toResponse(store);
    }

    @Override
    @Transactional
    public MerchantStoreResponse updateStore(Long merchantId, Long storeId, MerchantStoreRequest req) {
        ensureMerchantExists(merchantId);

        MerchantStore store = storeRepository.findByIdAndMerchantId(storeId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found for merchantId=" + merchantId + ", storeId=" + storeId
                ));

        // If storeCode is changed, enforce uniqueness
        if (!store.getStoreCode().equals(req.storeCode()) && storeRepository.existsByStoreCode(req.storeCode())) {
            throw new DuplicateResourceException("The store already exists (storeCode=" + req.storeCode() + ")");
        }

        store.setStoreName(req.storeName());
        store.setStoreCode(req.storeCode());
        store.setAddress(req.address());
        store.setDescription(req.description());
        store.setLogoUrl(req.logoUrl());
        store.setCurrency(req.currency());
        store.setDefaultLanguage(req.defaultLanguage());
        store.setIsActive(req.isActive() != null ? req.isActive() : store.getIsActive());

        //missing fields will not 'null' existing data
        if (req.description() != null) store.setDescription(req.description());
        if (req.logoUrl() != null) store.setLogoUrl(req.logoUrl());
        if (req.currency() != null) store.setCurrency(req.currency());
        if (req.defaultLanguage() != null) store.setDefaultLanguage(req.defaultLanguage());
        if (req.isActive() != null) store.setIsActive(req.isActive());

        MerchantStore saved = storeRepository.save(store);
        return toResponse(saved);
    }

    private void ensureMerchantExists(Long merchantId) {
        if (!merchantRepository.existsById(merchantId)) {
            throw new ResourceNotFoundException("Merchant not found: " + merchantId);
        }
    }

    private MerchantStoreResponse toResponse(MerchantStore store) {
        return new MerchantStoreResponse(
                store.getId(),
                store.getMerchant().getId(),
                store.getStoreName(),
                store.getStoreCode(),
                store.getDescription(),   
                store.getLogoUrl(),
                store.getAddress(),       
                store.getCurrency(),
                store.getDefaultLanguage(),
                store.getIsActive(),
                store.getCreatedAt(),
                store.getUpdatedAt()
        );
    }

    //list inventory for a merchant
    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getInventory(Long merchantId) {
        ensureMerchantExists(merchantId);

        return productRepository.findByStoreMerchantId(merchantId)
                .stream()
                .map(p -> new InventoryItemResponse(
                        p.getId(),
                        p.getSku(),
                        p.getName(),
                        p.getPrice(),
                        p.getStockQuantity(),
                        p.getStore().getId()
                ))
                .toList();
    }

    //update a product of a merchant
    @Override
    @Transactional
    public InventoryItemResponse updateProduct(Long merchantId, Long productId, InventoryUpdateRequest req) {
        ensureMerchantExists(merchantId);

        Product p = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

        // Ownership check (also handle null store to avoid NullPointerException)
        if (p.getStore() == null || p.getStore().getMerchant() == null ||
                !p.getStore().getMerchant().getId().equals(merchantId)) {
            throw new ResourceNotFoundException("Product not found for merchantId=" + merchantId);
        }

        boolean changed = false;

        if (req.stockQuantity() != null) {
            p.setStockQuantity(req.stockQuantity());
            changed = true;
        }
        if (req.price() != null) {
            p.setPrice(req.price());
            changed = true;
        }
        if (req.name() != null) {
            p.setName(req.name());
            changed = true;
        }
        if (req.description() != null) {
            p.setDescription(req.description());
            changed = true;
        }
        if (req.lowStockThreshold() != null) {
            p.setLowStockThreshold(req.lowStockThreshold());
            changed = true;
        }
        if (req.isActive() != null) {
            p.setIsActive(req.isActive());
            changed = true;
        }

        // If user sent {} or all nulls
        if (!changed) {
            throw new IllegalArgumentException("No fields provided to update");
            // If you have a BadRequestException class, use that instead.
        }

        Product saved = productRepository.save(p);

        return new InventoryItemResponse(
                saved.getId(),
                saved.getSku(),
                saved.getName(),
                saved.getPrice(),
                saved.getStockQuantity(),
                saved.getStore().getId()
        );
    }

    //get inventory by store
    @Override
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getInventoryByStore(Long merchantId, Long storeId) {
        ensureMerchantExists(merchantId);

        // ownership check: store belongs to merchant
        storeRepository.findByIdAndMerchantId(storeId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found for merchantId=" + merchantId + ", storeId=" + storeId
                ));

        return productRepository.findByStoreId(storeId).stream()
                .map(p -> new InventoryItemResponse(
                        p.getId(),
                        p.getSku(),
                        p.getName(),
                        p.getPrice(),
                        p.getStockQuantity(),
                        p.getStore() != null ? p.getStore().getId() : null
                ))
                .toList();
    }

    //create product for a store
    @Override
    @Transactional
    public InventoryItemResponse createProduct(Long merchantId, Long storeId, MerchantProductCreateRequest req) {
        ensureMerchantExists(merchantId);

        // store ownership check (merchant can only create under their own store)
        MerchantStore store = storeRepository.findByIdAndMerchantId(storeId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found for merchantId=" + merchantId + ", storeId=" + storeId
                ));

        // sku must be unique (Product has unique sku column)
        if (productRepository.findBySku(req.sku()).isPresent()) {
            throw new DuplicateResourceException("Product with same SKU already exists: " + req.sku());
        }

        Product p = new Product();
        p.setSku(req.sku());
        p.setName(req.name());
        p.setDescription(req.description());
        p.setPrice(req.price());

        // defaults if null
        p.setStockQuantity(req.stockQuantity() != null ? req.stockQuantity() : 0);
        p.setLowStockThreshold(req.lowStockThreshold() != null ? req.lowStockThreshold() : 10);
        p.setIsActive(req.isActive() != null ? req.isActive() : true);

        // link to store (requires Product has setStore + store_id in DB)
        p.setStore(store);

        // set category if provided
        // if (req.categoryId() != null) {
        //     Category cat = categoryRepository.findById(req.categoryId())
        //             .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + req.categoryId()));
        //     p.setCategory(cat);
        // } else {
        //     p.setCategory(null); // allow no category if your rules allow
        // }

        Product saved = productRepository.save(p);

        return new InventoryItemResponse(
                saved.getId(),
                saved.getSku(),
                saved.getName(),
                saved.getPrice(),
                saved.getStockQuantity(),
                saved.getStore().getId()
        );
    }

    //get sales report
    @Override
@Transactional(readOnly = true)
public SalesReportResponse getSalesReport(Long merchantId, Long storeId, LocalDate startDate, LocalDate endDate) {
    ensureMerchantExists(merchantId);

    if (storeId != null) {
        storeRepository.findByIdAndMerchantId(storeId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Store not found for merchantId=" + merchantId + ", storeId=" + storeId
                ));
    }

    LocalDateTime startTs = (startDate == null) ? null : startDate.atStartOfDay();
    LocalDateTime endTsExclusive = (endDate == null) ? null : endDate.plusDays(1).atStartOfDay();

    long totalOrders = reportRepository.countOrders(merchantId, storeId, startTs, endTsExclusive);
    BigDecimal totalRevenue = reportRepository.sumRevenue(merchantId, storeId, startTs, endTsExclusive);

    BigDecimal avg = BigDecimal.ZERO;
    if (totalOrders > 0) {
        avg = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
    }

    var topProducts = reportRepository.topSellingProducts(merchantId, storeId, startTs, endTsExclusive, 5);
    Map<String, BigDecimal> byDay = reportRepository.salesByDay(merchantId, storeId, startTs, endTsExclusive);

    Map<String, BigDecimal> byCategory = Collections.emptyMap(); // skip for now
    Double conversionRate = null; // not computable yet

    return new SalesReportResponse(
            storeId,
            startDate,
            endDate,
            totalOrders,
            totalRevenue,
            avg,
            topProducts,
            byCategory,
            byDay,
            conversionRate
    );
}


}
