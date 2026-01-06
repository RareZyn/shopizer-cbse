package com.shopizer.springboot.merchant.repository;

import com.shopizer.springboot.merchant.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Merchant Repository Interface  
 * FR-015: Merchant store profile management
 */
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {

    Optional<Merchant> findByEmail(String email);
}
