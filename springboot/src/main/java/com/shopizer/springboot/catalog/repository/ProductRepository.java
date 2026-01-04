package com.shopizer.springboot.catalog.repository;

import com.shopizer.springboot.catalog.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Product Repository Interface
 * FR-001: Product CRUD operations
 * FR-003: Search products by keyword
 * FR-005: Track product stock levels
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByNameContainingIgnoreCase(String keyword);

    List<Product> findByIsActiveTrue();

    List<Product> findByStockQuantityLessThanEqual(Integer threshold);
}
