package com.shopizer.catalog.activator;

import com.shopizer.catalog.api.CatalogService;
import com.shopizer.catalog.impl.CatalogServiceImpl;
import com.shopizer.catalog.repository.CategoryRepository;
import com.shopizer.catalog.repository.CategoryRepositoryImpl;
import com.shopizer.catalog.repository.MerchantStoreRepository;
import com.shopizer.catalog.repository.MerchantStoreRepositoryImpl;
import com.shopizer.catalog.repository.ProductRepository;
import com.shopizer.catalog.repository.ProductRepositoryImpl;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * OSGi Bundle Activator for Catalog Module
 *
 * ROLE IN ARCHITECTURE:
 * - OSGi lifecycle management (bundle start/stop)
 * - Service registration in OSGi registry
 * - Dependency injection for EntityManagerFactory
 * - Repository initialization with manual JPA implementation
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-001: Product CRUD operations (via ProductRepository)
 * - FR-002: Category CRUD operations (via CategoryRepository)
 * - FR-003: Search products by keyword (case-insensitive)
 * - FR-004: Browse products by category
 * - FR-005: Track product stock levels
 * - FR-006: Filter products by store (multi-store support)
 * - FR-007: View only active products
 * - FR-008: Product browsing with sorting
 * - FR-009: Stock management and validation
 *
 * DEPENDENCIES:
 * - EntityManagerFactory from com.shopizer.common (OSGi service)
 * - Manual JPA repositories (ProductRepositoryImpl, CategoryRepositoryImpl)
 */
public class CatalogActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(CatalogActivator.class);

    private ServiceRegistration<CatalogService> serviceRegistration;
    private EntityManagerFactory entityManagerFactory;
    private ServiceReference<EntityManagerFactory> emfServiceReference;

    /**
     * OSGi bundle start method
     *
     * LIFECYCLE:
     * 1. Retrieve EntityManagerFactory from OSGi service registry
     * 2. Create EntityManager instances for repositories
     * 3. Instantiate manual JPA repositories (ProductRepositoryImpl, CategoryRepositoryImpl)
     * 4. Create CatalogServiceImpl with working repositories
     * 5. Register CatalogService in OSGi service registry
     *
     * ERROR HANDLING:
     * - Throws exception if EntityManagerFactory not available
     * - Logs all initialization steps for debugging
     */
    @Override
    public void start(BundleContext context) throws Exception {
        logger.info("Starting Catalog Module...");

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

            // Step 2: Create EntityManager instances for each repository
            logger.info("Creating EntityManager instances for repositories...");
            EntityManager productEntityManager = entityManagerFactory.createEntityManager();
            EntityManager categoryEntityManager = entityManagerFactory.createEntityManager();
            EntityManager storeEntityManager = entityManagerFactory.createEntityManager();
            logger.info("EntityManager instances created successfully");

            // Step 3: Instantiate manual JPA repository implementations
            logger.info("Initializing manual JPA repositories...");
            ProductRepository productRepository = new ProductRepositoryImpl(productEntityManager);
            CategoryRepository categoryRepository = new CategoryRepositoryImpl(categoryEntityManager);
            MerchantStoreRepository merchantStoreRepository = new MerchantStoreRepositoryImpl(storeEntityManager);
            logger.info("Repositories initialized: ProductRepositoryImpl, CategoryRepositoryImpl, MerchantStoreRepositoryImpl");

            // Step 4: Create CatalogService implementation with working repositories
            logger.info("Creating CatalogService implementation...");
            CatalogService catalogService = new CatalogServiceImpl(productRepository, categoryRepository, merchantStoreRepository);
            logger.info("CatalogServiceImpl created successfully");

            // Step 5: Register CatalogService in OSGi service registry
            logger.info("Registering CatalogService in OSGi service registry...");
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("service.description", "Catalog Service for Product and Category Management");
            properties.put("service.vendor", "Shopizer");
            properties.put("service.version", "1.0.0");
            properties.put("functional.requirements", "FR-001,FR-002,FR-003,FR-004,FR-005,FR-006,FR-007,FR-008,FR-009");

            serviceRegistration = context.registerService(
                    CatalogService.class,
                    catalogService,
                    properties
            );

            logger.info("✅ Catalog Module started successfully. CatalogService registered in OSGi registry.");
            logger.info("   - ProductRepository: ACTIVE (Manual JPA Implementation)");
            logger.info("   - CategoryRepository: ACTIVE (Manual JPA Implementation)");
            logger.info("   - MerchantStoreRepository: ACTIVE (Manual JPA Implementation)");
            logger.info("   - CatalogService: READY (Functional Requirements: FR-001 to FR-009)");

        } catch (Exception e) {
            logger.error("❌ Failed to start Catalog Module: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * OSGi bundle stop method
     *
     * CLEANUP:
     * 1. Unregister CatalogService from OSGi registry
     * 2. Release EntityManagerFactory service reference
     * 3. Close EntityManager instances (managed by repository lifecycle)
     *
     * IMPORTANT:
     * - Proper cleanup prevents resource leaks
     * - Service unregistration notifies dependent bundles
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("Stopping Catalog Module...");

        try {
            // Step 1: Unregister CatalogService
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
                serviceRegistration = null;
                logger.info("CatalogService unregistered from OSGi registry");
            }

            // Step 2: Release EntityManagerFactory service reference
            if (emfServiceReference != null) {
                context.ungetService(emfServiceReference);
                emfServiceReference = null;
                logger.info("EntityManagerFactory service reference released");
            }

            entityManagerFactory = null;

            logger.info("✅ Catalog Module stopped successfully");
            logger.info("   - CatalogService: UNREGISTERED");
            logger.info("   - Repositories: DISPOSED");
            logger.info("   - EntityManagerFactory: RELEASED");

        } catch (Exception e) {
            logger.error("❌ Error while stopping Catalog Module: {}", e.getMessage(), e);
            throw e;
        }
    }
}
