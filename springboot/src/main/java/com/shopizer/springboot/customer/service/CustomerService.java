package com.shopizer.springboot.customer.service;

import com.shopizer.springboot.customer.entity.Customer;
import java.util.List;
import java.util.Optional;

/**
 * Customer Service Interface
 * FR-024 to FR-027: Customer management
 */
public interface CustomerService {

    Customer createCustomer(Customer customer);
    Optional<Customer> getCustomerById(Long id);
    Optional<Customer> getCustomerByEmail(String email);
    List<Customer> getAllCustomers();
    Customer updateCustomer(Long id, Customer customer);
    void deleteCustomer(Long id);
}
