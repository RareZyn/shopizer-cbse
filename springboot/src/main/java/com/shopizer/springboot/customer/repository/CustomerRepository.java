package com.shopizer.springboot.customer.repository;

import com.shopizer.springboot.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Customer Repository Interface
 * FR-024: The system shall allow customers to register and manage their accounts
 * FR-025: The system shall support customer authentication (login/logout)
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByEmail(String email);

    boolean existsByEmail(String email);
}
