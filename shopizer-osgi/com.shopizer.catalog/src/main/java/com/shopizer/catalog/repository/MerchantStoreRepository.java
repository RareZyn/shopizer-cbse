package com.shopizer.catalog.repository;

import com.shopizer.common.entity.MerchantStore;

import java.util.Optional;

/**
 * MerchantStoreRepository interface for catalog module
 * 
 * Note: This is a simplified interface that doesn't extend JpaRepository
 * because we only need findById for the catalog service.
 * The implementation uses manual JPA for OSGi compatibility.
 */
public interface MerchantStoreRepository {
    Optional<MerchantStore> findById(Long id);
}
