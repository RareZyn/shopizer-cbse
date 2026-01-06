package com.shopizer.springboot.merchant.service;

import com.shopizer.springboot.merchant.dto.MerchantStoreRequest;
import com.shopizer.springboot.merchant.dto.MerchantStoreResponse;
import com.shopizer.springboot.merchant.entity.Merchant;
import java.util.List;
import java.util.Optional;

/**
 * Merchant Service Interface
 * FR-015 to FR-018: Merchant management
 */
public interface MerchantService {

    Merchant createMerchant(Merchant merchant);
    Optional<Merchant> getMerchantById(Long id);
    List<Merchant> getAllMerchants();
    Merchant updateMerchant(Long id, Merchant merchant);
    void deleteMerchant(Long id);
    void deleteStore(Long merchantId, Long storeId);
    List<MerchantStoreResponse> listStores(Long merchantId);
    MerchantStoreResponse createStore(Long merchantId, MerchantStoreRequest req);
    MerchantStoreResponse getStore(Long merchantId, Long storeId);
    MerchantStoreResponse updateStore(Long merchantId, Long storeId, MerchantStoreRequest req);
}
