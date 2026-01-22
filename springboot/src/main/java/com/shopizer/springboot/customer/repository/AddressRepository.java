package com.shopizer.springboot.customer.repository;

import com.shopizer.springboot.customer.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Address Repository Interface
 * FR-026: The system shall allow customers to manage their addresses
 */
@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Find all addresses for a specific customer
     */
    List<Address> findByCustomerId(Long customerId);

    /**
     * Find default address for a customer
     */
    Optional<Address> findByCustomerIdAndIsDefaultTrue(Long customerId);

    /**
     * Find a specific address for a customer
     */
    Optional<Address> findByIdAndCustomerId(Long id, Long customerId);

    /**
     * Update all addresses for a customer to not be default
     */
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.customer.id = :customerId")
    void unsetDefaultForCustomer(@Param("customerId") Long customerId);

    /**
     * Check if address belongs to customer
     */
    boolean existsByIdAndCustomerId(Long id, Long customerId);
}
