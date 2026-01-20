package com.shopizer.merchant.repository;

import com.shopizer.common.entity.MerchantStore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantStoreRepository extends JpaRepository<MerchantStore, Long> {
    List<MerchantStore> findByMerchantId(Long merchantId);
    Optional<MerchantStore> findByIdAndMerchantId(Long id, Long merchantId);
}
