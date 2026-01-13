package com.shopizer.cart.activator;

import com.shopizer.cart.api.CartService;
import com.shopizer.cart.impl.CartServiceImpl;
import com.shopizer.cart.repository.CartItemRepository;
import com.shopizer.cart.repository.CartRepository;
import com.shopizer.catalog.api.CatalogService;
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
 * OSGI Bundle Activator for Cart Module
 * Manages lifecycle and service registration
 */
public class CartActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(CartActivator.class);

    private ServiceRegistration<CartService> serviceRegistration;
    private ServiceTracker<CatalogService, CatalogService> catalogServiceTracker;

    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Cart Module...");

        // Track CatalogService (dependency)
        catalogServiceTracker = new ServiceTracker<CatalogService, CatalogService>(
            context,
            CatalogService.class,
            null
        ) {
            @Override
            public CatalogService addingService(ServiceReference<CatalogService> reference) {
                CatalogService catalogService = super.addingService(reference);
                logger.info("CatalogService detected, registering CartService...");
                registerCartService(context, catalogService);
                return catalogService;
            }

            @Override
            public void removedService(ServiceReference<CatalogService> reference, CatalogService service) {
                logger.info("CatalogService removed, unregistering CartService...");
                unregisterCartService();
                super.removedService(reference, service);
            }
        };

        catalogServiceTracker.open();
        logger.info("Cart Module started. Waiting for CatalogService...");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Cart Module...");

        if (catalogServiceTracker != null) {
            catalogServiceTracker.close();
        }

        unregisterCartService();

        logger.info("Cart Module stopped successfully.");
    }

    /**
     * Register CartService in OSGI registry
     */
    private void registerCartService(BundleContext context, CatalogService catalogService) {
        if (serviceRegistration != null) {
            logger.warn("CartService already registered, skipping...");
            return;
        }

        try {
            // TODO: Initialize JPA repositories (will be configured via Spring Data JPA OSGI support)
            // For now, we'll create placeholder implementations
            CartRepository cartRepository = null; // Will be injected by OSGI framework
            CartItemRepository cartItemRepository = null; // Will be injected by OSGI framework

            // Create service implementation
            CartService cartService = new CartServiceImpl(
                cartRepository,
                cartItemRepository,
                catalogService
            );

            // Register service in OSGI registry
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("service.description", "Cart Service for Shopping Cart Management");
            properties.put("service.vendor", "Shopizer");
            properties.put("service.version", "1.0.0");

            serviceRegistration = context.registerService(
                CartService.class,
                cartService,
                properties
            );

            logger.info("CartService registered successfully.");
        } catch (Exception e) {
            logger.error("Failed to register CartService", e);
        }
    }

    /**
     * Unregister CartService from OSGI registry
     */
    private void unregisterCartService() {
        if (serviceRegistration != null) {
            try {
                serviceRegistration.unregister();
                serviceRegistration = null;
                logger.info("CartService unregistered.");
            } catch (IllegalStateException e) {
                logger.warn("CartService already unregistered.");
            }
        }
    }
}
