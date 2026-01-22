package com.shopizer.catalog.repository;

import com.shopizer.common.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySku(String sku);
    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStoreId(Long storeId);
    List<Product> findByActiveTrue();

    /**
     * FR-005: Find products with low stock
     * Returns products where stockQuantity <= reorderLevel (low_stock_threshold)
     */
    List<Product> findLowStockProducts();
}
