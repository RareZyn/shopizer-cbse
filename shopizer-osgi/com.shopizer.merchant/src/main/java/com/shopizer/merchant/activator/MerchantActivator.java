package com.shopizer.merchant.activator;

import com.shopizer.catalog.api.CatalogService;
import com.shopizer.merchant.api.MerchantService;
import com.shopizer.merchant.impl.MerchantServiceImpl;
import com.shopizer.merchant.repository.MerchantStoreRepository;
import com.shopizer.order.api.OrderService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * OSGI Bundle Activator for Merchant Module
 * Manages lifecycle and service registration
 * Depends on CatalogService and OrderService
 */
public class MerchantActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(MerchantActivator.class);

    private ServiceRegistration<MerchantService> serviceRegistration;

    private ServiceTracker<CatalogService, CatalogService> catalogServiceTracker;
    private ServiceTracker<OrderService, OrderService> orderServiceTracker;

    private CatalogService catalogService;
    private OrderService orderService;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Merchant Module...");

        // Track CatalogService (dependency)
        catalogServiceTracker = new ServiceTracker<CatalogService, CatalogService>(
            context,
            CatalogService.class,
            null
        ) {
            @Override
            public CatalogService addingService(ServiceReference<CatalogService> reference) {
                CatalogService service = super.addingService(reference);
                logger.info("CatalogService detected");
                catalogService = service;
                tryRegisterMerchantService(context);
                return service;
            }

            @Override
            public void removedService(ServiceReference<CatalogService> reference, CatalogService service) {
                logger.info("CatalogService removed, unregistering MerchantService...");
                catalogService = null;
                unregisterMerchantService();
                super.removedService(reference, service);
            }
        };

        // Track OrderService (dependency)
        orderServiceTracker = new ServiceTracker<OrderService, OrderService>(
            context,
            OrderService.class,
            null
        ) {
            @Override
            public OrderService addingService(ServiceReference<OrderService> reference) {
                OrderService service = super.addingService(reference);
                logger.info("OrderService detected");
                orderService = service;
                tryRegisterMerchantService(context);
                return service;
            }

            @Override
            public void removedService(ServiceReference<OrderService> reference, OrderService service) {
                logger.info("OrderService removed, unregistering MerchantService...");
                orderService = null;
                unregisterMerchantService();
                super.removedService(reference, service);
            }
        };

        catalogServiceTracker.open();
        orderServiceTracker.open();

        logger.info("Merchant Module started. Waiting for CatalogService and OrderService...");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Merchant Module...");

        if (catalogServiceTracker != null) {
            catalogServiceTracker.close();
        }

        if (orderServiceTracker != null) {
            orderServiceTracker.close();
        }

        unregisterMerchantService();

        logger.info("Merchant Module stopped successfully.");
    }

    /**
     * Try to register MerchantService if all dependencies are available
     */
    private synchronized void tryRegisterMerchantService(BundleContext context) {
        if (serviceRegistration != null) {
            logger.debug("MerchantService already registered");
            return;
        }

        if (catalogService == null || orderService == null) {
            logger.debug("Not all dependencies available yet. CatalogService: {}, OrderService: {}",
                catalogService != null, orderService != null);
            return;
        }

        logger.info("All dependencies available, registering MerchantService...");
        registerMerchantService(context);
    }

    /**
     * Register MerchantService in OSGI registry
     */
    private void registerMerchantService(BundleContext context) {
        try {
            // TODO: Initialize JPA repositories (will be configured via Spring Data JPA OSGI support)
            // For now, we'll create placeholder implementations
            MerchantStoreRepository merchantStoreRepository = null; // Will be injected by OSGI framework

            // Create service implementation
            MerchantService merchantService = new MerchantServiceImpl(
                merchantStoreRepository,
                catalogService,
                orderService
            );

            // Register service in OSGI registry
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("service.description", "Merchant Service for Store Management, Inventory, and Analytics");
            properties.put("service.vendor", "Shopizer");
            properties.put("service.version", "1.0.0");

            serviceRegistration = context.registerService(
                MerchantService.class,
                merchantService,
                properties
            );

            logger.info("MerchantService registered successfully.");
        } catch (Exception e) {
            logger.error("Failed to register MerchantService", e);
        }
    }

    /**
     * Unregister MerchantService from OSGI registry
     */
    private synchronized void unregisterMerchantService() {
        if (serviceRegistration != null) {
            try {
                serviceRegistration.unregister();
                serviceRegistration = null;
                logger.info("MerchantService unregistered.");
            } catch (IllegalStateException e) {
                logger.warn("MerchantService already unregistered.");
            }
        }
    }
}
