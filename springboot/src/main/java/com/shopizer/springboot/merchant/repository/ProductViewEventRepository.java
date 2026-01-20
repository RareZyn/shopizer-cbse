package com.shopizer.springboot.merchant.repository;

import com.shopizer.springboot.merchant.entity.ProductViewEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for product view tracking (FR-018)
 */
@Repository
public interface ProductViewEventRepository extends JpaRepository<ProductViewEvent, Long> {
}
