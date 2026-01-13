package com.shopizer.customer.activator;

import com.shopizer.common.util.JwtTokenProvider;
import com.shopizer.customer.api.CustomerService;
import com.shopizer.customer.impl.CustomerServiceImpl;
import com.shopizer.customer.repository.AddressRepository;
import com.shopizer.customer.repository.CustomerRepository;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * OSGI Bundle Activator for Customer Module
 * Manages lifecycle and service registration
 */
public class CustomerActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(CustomerActivator.class);

    private ServiceRegistration<CustomerService> serviceRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Customer Module...");

        registerCustomerService(context);

        logger.info("Customer Module started successfully.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Customer Module...");

        unregisterCustomerService();

        logger.info("Customer Module stopped successfully.");
    }

    /**
     * Register CustomerService in OSGI registry
     */
    private void registerCustomerService(BundleContext context) {
        try {
            // TODO: Initialize JPA repositories (will be configured via Spring Data JPA OSGI support)
            // For now, we'll create placeholder implementations
            CustomerRepository customerRepository = null; // Will be injected by OSGI framework
            AddressRepository addressRepository = null; // Will be injected by OSGI framework

            // Initialize JWT token provider
            // TODO: Load secret key from configuration
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
                "your-256-bit-secret-key-change-this-in-production",
                86400000 // 24 hours
            );

            // Create service implementation
            CustomerService customerService = new CustomerServiceImpl(
                customerRepository,
                addressRepository,
                jwtTokenProvider
            );

            // Register service in OSGI registry
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("service.description", "Customer Service for Authentication and Profile Management");
            properties.put("service.vendor", "Shopizer");
            properties.put("service.version", "1.0.0");

            serviceRegistration = context.registerService(
                CustomerService.class,
                customerService,
                properties
            );

            logger.info("CustomerService registered successfully.");
        } catch (Exception e) {
            logger.error("Failed to register CustomerService", e);
        }
    }

    /**
     * Unregister CustomerService from OSGI registry
     */
    private void unregisterCustomerService() {
        if (serviceRegistration != null) {
            try {
                serviceRegistration.unregister();
                serviceRegistration = null;
                logger.info("CustomerService unregistered.");
            } catch (IllegalStateException e) {
                logger.warn("CustomerService already unregistered.");
            }
        }
    }
}
