package com.shopizer.merchant.repository;

import com.shopizer.common.entity.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FR-018: Product View Repository
 * Provides access to product view data for conversion tracking and analytics
 */
public interface ProductViewRepository extends JpaRepository<ProductView, Long> {

    /**
     * Count views for a product within a date range
     */
    long countByProductIdAndViewedAtBetween(Long productId, LocalDateTime start, LocalDateTime end);

    /**
     * Get all views for a product within a date range
     */
    List<ProductView> findByProductIdAndViewedAtBetween(Long productId, LocalDateTime start, LocalDateTime end);

    /**
     * Count total views for a product
     */
    long countByProductId(Long productId);

    /**
     * Get views by store and date range
     */
    @Query("SELECT pv FROM ProductView pv WHERE pv.store.id = :storeId AND pv.viewedAt >= :start AND pv.viewedAt <= :end")
    List<ProductView> findByStoreIdAndDateRange(@Param("storeId") Long storeId,
                                                  @Param("start") LocalDateTime start,
                                                  @Param("end") LocalDateTime end);
}
