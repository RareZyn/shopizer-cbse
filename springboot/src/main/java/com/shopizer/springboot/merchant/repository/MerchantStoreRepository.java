package com.shopizer.springboot.merchant.repository;
import com.shopizer.springboot.merchant.entity.MerchantStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
/**
 * Merchant Store Repository Interface
 * FR-015: The system shall allow merchants to manage their store profile
 * FR-016: The system shall allow merchants to manage their product inventory
 */
public interface MerchantStoreRepository extends JpaRepository<MerchantStore, Long> {

    List<MerchantStore> findByMerchantId(Long merchantId);

    Optional<MerchantStore> findByIdAndMerchantId(Long storeId, Long merchantId);

    boolean existsByStoreCode(String storeCode);
}
