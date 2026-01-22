package com.shopizer.cart.activator;

import com.shopizer.cart.api.CartService;
import com.shopizer.cart.impl.CartServiceImpl;
import com.shopizer.cart.repository.CartItemRepository;
import com.shopizer.cart.repository.CartItemRepositoryImpl;
import com.shopizer.cart.repository.CartRepository;
import com.shopizer.cart.repository.CartRepositoryImpl;
import com.shopizer.catalog.api.CatalogService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
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
 * OSGi Bundle Activator for Cart Module
 *
 * ROLE IN ARCHITECTURE:
 * - OSGi lifecycle management (bundle start/stop)
 * - Service registration in OSGi registry
 * - Dependency injection for EntityManagerFactory and CatalogService
 * - Repository initialization with manual JPA implementation
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: Add items to cart
 * - FR-007: View cart contents
 * - FR-008: Update/remove cart items
 * - FR-009: Cart validation for checkout
 *
 * DEPENDENCIES:
 * - EntityManagerFactory from com.shopizer.common (OSGi service)
 * - CatalogService from com.shopizer.catalog (OSGi service)
 * - Manual JPA repositories (CartRepositoryImpl, CartItemRepositoryImpl)
 */
public class CartActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(CartActivator.class);

    private ServiceRegistration<CartService> serviceRegistration;
    private ServiceTracker<CatalogService, CatalogService> catalogServiceTracker;
    private EntityManagerFactory entityManagerFactory;
    private ServiceReference<EntityManagerFactory> emfServiceReference;

    /**
     * OSGi bundle start method
     *
     * LIFECYCLE:
     * 1. Retrieve EntityManagerFactory from OSGi service registry
     * 2. Track CatalogService availability (dependency)
     * 3. When CatalogService becomes available:
     *    - Create EntityManager instances for repositories
     *    - Instantiate manual JPA repositories (CartRepositoryImpl, CartItemRepositoryImpl)
     *    - Create CartServiceImpl with working repositories and CatalogService
     *    - Register CartService in OSGi service registry
     *
     * ERROR HANDLING:
     * - Throws exception if EntityManagerFactory not available
     * - Waits for CatalogService before registering CartService
     * - Logs all initialization steps for debugging
     */
    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Cart Module...");

        try {
            // Step 1: Get EntityManagerFactory from OSGi service registry
            logger.info("Looking up EntityManagerFactory from OSGi service registry...");
            emfServiceReference = context.getServiceReference(EntityManagerFactory.class);

            if (emfServiceReference == null) {
                throw new IllegalStateException("EntityManagerFactory service not found in OSGi registry. " +
                    "Ensure com.shopizer.common bundle is started first.");
            }

            entityManagerFactory = context.getService(emfServiceReference);
            logger.info("EntityManagerFactory retrieved successfully from OSGi registry");

            // Step 2: Track CatalogService (dependency)
            logger.info("Setting up CatalogService tracker...");
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

        } catch (Exception e) {
            logger.error("❌ Failed to start Cart Module: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * OSGi bundle stop method
     *
     * CLEANUP:
     * 1. Close CatalogService tracker
     * 2. Unregister CartService from OSGi registry
     * 3. Release EntityManagerFactory service reference
     *
     * IMPORTANT:
     * - Proper cleanup prevents resource leaks
     * - Service unregistration notifies dependent bundles
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Cart Module...");

        try {
            // Step 1: Close CatalogService tracker
            if (catalogServiceTracker != null) {
                catalogServiceTracker.close();
                catalogServiceTracker = null;
                logger.info("CatalogService tracker closed");
            }

            // Step 2: Unregister CartService
            unregisterCartService();

            // Step 3: Release EntityManagerFactory service reference
            if (emfServiceReference != null) {
                context.ungetService(emfServiceReference);
                emfServiceReference = null;
                logger.info("EntityManagerFactory service reference released");
            }

            entityManagerFactory = null;

            logger.info("✅ Cart Module stopped successfully");
            logger.info("   - CartService: UNREGISTERED");
            logger.info("   - Repositories: DISPOSED");
            logger.info("   - EntityManagerFactory: RELEASED");

        } catch (Exception e) {
            logger.error("❌ Error while stopping Cart Module: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Register CartService in OSGi registry
     *
     * INITIALIZATION:
     * 1. Create EntityManager instances for each repository
     * 2. Instantiate CartRepositoryImpl and CartItemRepositoryImpl
     * 3. Create CartServiceImpl with repositories and CatalogService
     * 4. Register CartService with service properties
     */
    private void registerCartService(BundleContext context, CatalogService catalogService) {
        if (serviceRegistration != null) {
            logger.warn("CartService already registered, skipping...");
            return;
        }

        try {
            // Step 1: Create EntityManager instances for each repository
            logger.info("Creating EntityManager instances for repositories...");
            EntityManager cartEntityManager = entityManagerFactory.createEntityManager();
            EntityManager cartItemEntityManager = entityManagerFactory.createEntityManager();
            logger.info("EntityManager instances created successfully");

            // Step 2: Instantiate manual JPA repository implementations
            logger.info("Initializing manual JPA repositories...");
            CartRepository cartRepository = new CartRepositoryImpl(cartEntityManager);
            CartItemRepository cartItemRepository = new CartItemRepositoryImpl(cartItemEntityManager);
            logger.info("Repositories initialized: CartRepositoryImpl, CartItemRepositoryImpl");

            // Step 3: Create CartService implementation with working repositories and CatalogService
            logger.info("Creating CartService implementation...");
            CartService cartService = new CartServiceImpl(
                cartRepository,
                cartItemRepository,
                catalogService
            );
            logger.info("CartServiceImpl created successfully");

            // Step 4: Register CartService in OSGi service registry
            logger.info("Registering CartService in OSGi service registry...");
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("service.description", "Cart Service for Shopping Cart Management");
            properties.put("service.vendor", "Shopizer");
            properties.put("service.version", "1.0.0");
            properties.put("functional.requirements", "FR-006,FR-007,FR-008,FR-009");

            serviceRegistration = context.registerService(
                CartService.class,
                cartService,
                properties
            );

            logger.info("✅ Cart Module initialized successfully. CartService registered in OSGi registry.");
            logger.info("   - CartRepository: ACTIVE (Manual JPA Implementation)");
            logger.info("   - CartItemRepository: ACTIVE (Manual JPA Implementation)");
            logger.info("   - CartService: READY (Functional Requirements: FR-006 to FR-009)");

        } catch (Exception e) {
            logger.error("❌ Failed to register CartService: {}", e.getMessage(), e);
        }
    }

    /**
     * Unregister CartService from OSGi registry
     */
    private void unregisterCartService() {
        if (serviceRegistration != null) {
            try {
                serviceRegistration.unregister();
                serviceRegistration = null;
                logger.info("CartService unregistered from OSGi registry");
            } catch (IllegalStateException e) {
                logger.warn("CartService already unregistered.");
            }
        }
    }
}
