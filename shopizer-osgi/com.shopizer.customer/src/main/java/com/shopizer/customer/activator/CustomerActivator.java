package com.shopizer.customer.activator;

import com.shopizer.common.util.JwtTokenProvider;
import com.shopizer.customer.api.CustomerService;
import com.shopizer.customer.impl.CustomerServiceImpl;
import com.shopizer.customer.repository.AddressRepository;
import com.shopizer.customer.repository.AddressRepositoryImpl;
import com.shopizer.customer.repository.CustomerRepository;
import com.shopizer.customer.repository.CustomerRepositoryImpl;
import jakarta.persistence.EntityManagerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class CustomerActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(CustomerActivator.class);
    private ServiceRegistration<CustomerService> serviceRegistration;
    private EntityManagerFactory entityManagerFactory;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Customer Module...");

        // Get EntityManagerFactory from OSGI service registry
        ServiceReference<EntityManagerFactory> emfRef = context.getServiceReference(EntityManagerFactory.class);
        if (emfRef == null) {
            throw new RuntimeException("EntityManagerFactory service not found! Make sure common module is started first.");
        }
        
        entityManagerFactory = context.getService(emfRef);
        logger.info("EntityManagerFactory service retrieved successfully");

        // Initialize JPA repositories with EntityManagerFactory
        CustomerRepository customerRepository = new CustomerRepositoryImpl(entityManagerFactory);
        AddressRepository addressRepository = new AddressRepositoryImpl(entityManagerFactory);
        logger.info("Repository implementations created successfully");

        // Initialize JWT token provider
        // TODO: Load secret key from configuration
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            "your-256-bit-secret-key-change-this-in-production-minimum-32-characters"
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

        serviceRegistration = context.registerService(
            CustomerService.class,
            customerService,
            properties
        );

        logger.info("Customer Module started successfully. Service registered.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Customer Module...");

        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            logger.info("Customer Service unregistered.");
        }

        logger.info("Customer Module stopped successfully.");
    }
}
