package com.shopizer.merchant.repository;

import com.shopizer.common.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    
    List<Product> findByStoreId(Long storeId);
    
    List<Product> findByStoreIdAndActive(Long storeId, Boolean active);
    
    @Query("SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity <= p.reorderLevel AND p.active = true")
    List<Product> findLowStockProducts(@Param("storeId") Long storeId);
    
    @Query("SELECT p FROM Product p WHERE p.store.merchant.id = :merchantId AND p.stockQuantity <= p.reorderLevel AND p.active = true")
    List<Product> findLowStockProductsByMerchant(@Param("merchantId") Long merchantId);
}
