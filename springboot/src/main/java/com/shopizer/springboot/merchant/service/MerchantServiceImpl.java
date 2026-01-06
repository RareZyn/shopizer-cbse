package com.shopizer.springboot.merchant.service;

import com.shopizer.springboot.merchant.entity.Merchant;
import com.shopizer.springboot.merchant.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import com.shopizer.springboot.merchant.dto.MerchantStoreRequest;
import com.shopizer.springboot.merchant.dto.MerchantStoreResponse;
// import com.shopizer.springboot.merchant.entity.Merchant;
import com.shopizer.springboot.merchant.entity.MerchantStore;
import com.shopizer.springboot.merchant.exception.DuplicateResourceException;
import com.shopizer.springboot.merchant.exception.ResourceNotFoundException;
// import com.shopizer.springboot.merchant.repository.MerchantRepository;
import com.shopizer.springboot.merchant.repository.MerchantStoreRepository;
// import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Merchant Service Implementation
 * FR-015 to FR-018: Merchant functionality
 */
@Service
public class MerchantServiceImpl implements MerchantService {

    private final MerchantRepository merchantRepository;
    private final MerchantStoreRepository storeRepository;

    public MerchantServiceImpl(MerchantRepository merchantRepository, MerchantStoreRepository storeRepository) {
        this.merchantRepository = merchantRepository;
        this.storeRepository = storeRepository;
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
}
