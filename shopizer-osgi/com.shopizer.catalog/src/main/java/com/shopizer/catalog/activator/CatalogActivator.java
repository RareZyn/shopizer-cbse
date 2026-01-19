package com.shopizer.catalog.activator;

import com.shopizer.catalog.api.CatalogService;
import com.shopizer.catalog.impl.CatalogServiceImpl;
import com.shopizer.catalog.repository.CategoryRepository;
import com.shopizer.catalog.repository.ProductRepository;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

public class CatalogActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(CatalogActivator.class);
    private ServiceRegistration<CatalogService> serviceRegistration;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Catalog Module...");

        // TODO: Initialize JPA repositories (will be configured via Spring Data JPA OSGI support)
        // For now, we'll create placeholder implementations
        ProductRepository productRepository = null; // Will be injected by OSGI framework
        CategoryRepository categoryRepository = null; // Will be injected by OSGI framework

        // Create service implementation
        CatalogService catalogService = new CatalogServiceImpl(productRepository, categoryRepository);

        // Register service in OSGI registry
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("service.description", "Catalog Service for Product and Category Management");
        properties.put("service.vendor", "Shopizer");

        serviceRegistration = context.registerService(
                CatalogService.class,
                catalogService,
                properties
        );

        logger.info("Catalog Module started successfully. Service registered.");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Catalog Module...");

        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            logger.info("Catalog Service unregistered.");
        }

        logger.info("Catalog Module stopped successfully.");
    }
}
