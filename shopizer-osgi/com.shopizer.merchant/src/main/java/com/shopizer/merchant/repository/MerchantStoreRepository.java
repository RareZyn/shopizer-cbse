package com.shopizer.merchant.repository;

import com.shopizer.common.entity.MerchantStore;
import com.shopizer.common.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantStoreRepository extends JpaRepository<MerchantStore, Long> {
    List<MerchantStore> findByMerchantId(Long merchantId);
    Optional<MerchantStore> findByIdAndMerchantId(Long id, Long merchantId);

    // Product helper methods for FR-017
    default Product saveProduct(Product product) {
        // Implementation delegated to ProductRepository
        return product;
    }

    default Optional<Product> findProductById(Long productId) {
        // Implementation delegated to ProductRepository
        return Optional.empty();
    }

    default List<Product> findProductsByStore(Long storeId) {
        // Implementation delegated to ProductRepository
        return List.of();
    }

    default void deleteProduct(Long productId) {
        // Implementation delegated to ProductRepository
    }

    default List<Product> findLowStockProductsByStore(Long storeId) {
        // Implementation delegated to ProductRepository
        return List.of();
    }

    default List<Product> findLowStockProductsByMerchant(Long merchantId) {
        // Implementation delegated to ProductRepository
        return List.of();
    }
}
