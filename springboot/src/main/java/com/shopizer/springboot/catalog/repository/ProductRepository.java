package com.shopizer.springboot.catalog.repository;

import com.shopizer.springboot.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Product Repository Interface
 * FR-001: Product CRUD operations
 * FR-003: Search products by keyword
 * FR-005: Track product stock levels
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContaining(String keyword);

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByIsActiveTrue();

    List<Product> findByStockQuantityLessThanEqual(Integer threshold);

    List<Product> findByCategoryId(Long categoryId);

    Optional<Product> findBySku(String sku);

    List<Product> findByStoreId(Long storeId); // Find products by store ID - added by yourisha
    List<Product> findByStoreMerchantId(Long merchantId); // Find products by merchant ID through store - added by yourisha


    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category")
    List<Product> findAllWithCategory();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.isActive = true")
    List<Product> findAllActiveWithCategory();

    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN FETCH p.category WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> findByNameContainingIgnoreCaseWithCategory(String keyword);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.stockQuantity <= :threshold")
    List<Product> findByStockQuantityLessThanEqualWithCategory(Integer threshold);

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.lowStockThreshold IS NOT NULL AND p.stockQuantity <= p.lowStockThreshold")
    List<Product> findLowStockProductsWithCategory();

    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId")
    List<Product> findByCategoryIdWithCategory(Long categoryId);
}