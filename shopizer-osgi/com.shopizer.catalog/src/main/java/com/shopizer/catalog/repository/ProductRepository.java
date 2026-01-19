package com.shopizer.catalog.repository;

import com.shopizer.common.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String keyword);
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByStoreId(Long storeId);
    List<Product> findByActiveTrue();
}
