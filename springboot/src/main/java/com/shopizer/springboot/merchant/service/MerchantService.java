package com.shopizer.springboot.merchant.service;

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
}
