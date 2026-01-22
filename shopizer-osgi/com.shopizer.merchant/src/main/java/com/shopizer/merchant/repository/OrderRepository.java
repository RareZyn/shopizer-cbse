package com.shopizer.merchant.repository;

import com.shopizer.common.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FR-018: Order Repository Interface
 * Provides access to order data for sales reporting and analytics
 * 
 * NOTE: This is the interface definition for Spring Data JPA.
 * The OSGi module will provide a manual implementation via OrderRepositoryImpl.
 */
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders for a specific customer
     */
    List<Order> findByCustomerId(Long customerId);

    /**
     * Find orders created within a date range
     */
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    List<Order> findOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Find orders by status
     */
    List<Order> findByStatus(String status);

    /**
     * Find orders for a specific store
     */
    List<Order> findByStoreId(Long storeId);

    /**
     * Find orders for a customer within a date range
     */
    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdAndDateRange(@Param("customerId") Long customerId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Find completed/paid orders within date range
     */
    @Query("SELECT o FROM Order o WHERE (o.status = 'PAID' OR o.status = 'PROCESSING' OR o.status = 'SHIPPED' OR o.status = 'DELIVERED') AND o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    List<Order> findCompletedOrdersByDateRange(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Find an order by its order number
     */
    java.util.Optional<Order> findByOrderNumber(String orderNumber);
}
