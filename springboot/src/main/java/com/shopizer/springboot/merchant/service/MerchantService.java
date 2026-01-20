package com.shopizer.springboot.merchant.service;

import com.shopizer.springboot.merchant.dto.InventoryItemResponse;
import com.shopizer.springboot.merchant.dto.InventoryUpdateRequest;
import com.shopizer.springboot.merchant.dto.MerchantAuthResponse;
import com.shopizer.springboot.merchant.dto.MerchantLoginRequest;
import com.shopizer.springboot.merchant.dto.MerchantRegisterRequest;
import com.shopizer.springboot.merchant.dto.MerchantStoreRequest;
import com.shopizer.springboot.merchant.dto.MerchantStoreResponse;
import com.shopizer.springboot.merchant.dto.ProductAnalyticsResponse;
import com.shopizer.springboot.merchant.dto.ProductReportResponse;
import com.shopizer.springboot.merchant.dto.SalesReportResponse;
import com.shopizer.springboot.merchant.entity.Merchant;
import com.shopizer.springboot.merchant.dto.InventoryItemResponse;
import com.shopizer.springboot.merchant.dto.MerchantProductCreateRequest;
import com.shopizer.springboot.merchant.dto.SalesReportResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Merchant Service Interface
 * FR-015 to FR-018: Merchant management
 */
@SuppressWarnings("unused")
public interface MerchantService {

    Merchant createMerchant(Merchant merchant);
    Optional<Merchant> getMerchantById(Long id);
    List<Merchant> getAllMerchants();
    Merchant updateMerchant(Long id, Merchant merchant);
    void deleteMerchant(Long id);
    MerchantAuthResponse register(MerchantRegisterRequest req);
    MerchantAuthResponse login(MerchantLoginRequest req);
    void deleteStore(Long merchantId, Long storeId);
    List<MerchantStoreResponse> listStores(Long merchantId);
    MerchantStoreResponse createStore(Long merchantId, MerchantStoreRequest req);
    MerchantStoreResponse getStore(Long merchantId, Long storeId);
    InventoryItemResponse createProduct(Long merchantId, Long storeId, MerchantProductCreateRequest req);//create product for a store
    MerchantStoreResponse updateStore(Long merchantId, Long storeId, MerchantStoreRequest req);
    List<InventoryItemResponse> getInventory(Long merchantId); //get inventory regardless store
    List<InventoryItemResponse> getInventoryByStore(Long merchantId, Long storeId); //get inventory by store
    InventoryItemResponse updateProduct(Long merchantId, Long productId, InventoryUpdateRequest req);
    SalesReportResponse getSalesReport(Long merchantId, Long storeId, LocalDate startDate, LocalDate endDate); //get sales report
    List<ProductReportResponse> getProductReport(Long merchantId, Long storeId, Long categoryId, Long productId, LocalDate startDate, LocalDate endDate);
    ProductAnalyticsResponse getProductAnalytics(Long merchantId, Long productId, LocalDate startDate, LocalDate endDate);
    void recordProductView(Long merchantId, Long productId);
    void deleteProduct(Long merchantId, Long productId);
    List<InventoryItemResponse> getLowStockProducts(Long merchantId, Long storeId);
    
}
