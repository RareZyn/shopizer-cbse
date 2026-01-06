package com.shopizer.springboot.catalog.activator;

import com.shopizer.springboot.catalog.service.CatalogService;
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
 * This activator registers the CatalogService as an OSGi service
 * when the bundle is started and unregisters it when the bundle is stopped.
 * 
 * In a Spring Boot + OSGi hybrid environment, this activator:
 * 1. Looks up the CatalogService from Spring ApplicationContext (if available)
 * 2. Registers it as an OSGi service
 * 3. Allows other OSGi bundles to consume the CatalogService
 * 
 * Component-Based Software Engineering (CBSE) - Catalog Module Bundle
 * 
 * Bundle Symbolic Name: com.shopizer.springboot.catalog
 * Bundle Version: 1.0.0
 */
public class CatalogBundleActivator implements BundleActivator {

    private static final Logger logger = LoggerFactory.getLogger(CatalogBundleActivator.class);
    
    private ServiceRegistration<CatalogService> serviceRegistration;
    private CatalogService catalogService;
    private BundleContext bundleContext;

    /**
     * Called when the OSGi bundle is started.
     * Registers the CatalogService in the OSGi service registry.
     * 
     * @param context The bundle context for this bundle
     */
    @Override
    public void start(BundleContext context) throws Exception {
        this.bundleContext = context;
        logger.info("========================================");
        logger.info("Starting Catalog Module OSGi Bundle");
        logger.info("Bundle Symbolic Name: {}", context.getBundle().getSymbolicName());
        logger.info("Bundle Version: {}", context.getBundle().getVersion());
        logger.info("========================================");
        
        try {
            // Try to get CatalogService from Spring ApplicationContext
            // In a Spring Boot environment, services are managed by Spring
            catalogService = lookupCatalogServiceFromSpring(context);
            
            if (catalogService == null) {
                logger.warn("CatalogService not found in Spring context. " +
                           "This bundle may need Spring Boot to be fully initialized.");
                // For demonstration purposes, we'll create a placeholder
                // In production, you would wait for Spring to initialize
                return;
            }
            
            // Create service properties
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("service.description", "Catalog Service - Product and Category Management");
            properties.put("service.vendor", "Shopizer CBSE");
            properties.put("module.name", "catalog");
            properties.put("functional.requirements", "FR-001,FR-002,FR-003,FR-004,FR-005");
            
            // Register the service with OSGi service registry
            serviceRegistration = context.registerService(
                CatalogService.class,
                catalogService,
                properties
            );
            
            logger.info("✓ CatalogService registered in OSGi service registry");
            logger.info("  Service Interface: {}", CatalogService.class.getName());
            logger.info("  Service Properties: {}", properties);
            logger.info("========================================");
            
        } catch (Exception e) {
            logger.error("✗ Error starting Catalog Module OSGi Bundle", e);
            throw e;
        }
    }

    /**
     * Called when the OSGi bundle is stopped.
     * Unregisters the CatalogService from the OSGi service registry.
     * 
     * @param context The bundle context for this bundle
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        logger.info("========================================");
        logger.info("Stopping Catalog Module OSGi Bundle");
        logger.info("========================================");
        
        try {
            // Unregister the service
            if (serviceRegistration != null) {
                serviceRegistration.unregister();
                logger.info("✓ CatalogService unregistered from OSGi service registry");
            }
            
            // Clean up service instance
            catalogService = null;
            bundleContext = null;
            
            logger.info("✓ Catalog Module OSGi Bundle stopped successfully");
            logger.info("========================================");
            
        } catch (Exception e) {
            logger.error("✗ Error stopping Catalog Module OSGi Bundle", e);
            throw e;
        }
    }

    /**
     * Looks up CatalogService from Spring ApplicationContext.
     * This method attempts to find the service through OSGi service registry
     * or directly from Spring if the context is available.
     * 
     * @param context The bundle context
     * @return CatalogService instance or null if not found
     */
    private CatalogService lookupCatalogServiceFromSpring(BundleContext context) {
        try {
            // Method 1: Try to get from OSGi service registry (if Spring Boot registered it)
            ServiceReference<?>[] refs = context.getServiceReferences(
                CatalogService.class.getName(), 
                null
            );
            
            if (refs != null && refs.length > 0) {
                CatalogService service = (CatalogService) context.getService(refs[0]);
                logger.info("Found CatalogService in OSGi service registry");
                return service;
            }
            
            // Method 2: In a Spring Boot + OSGi hybrid, you might access Spring context
            // This is a placeholder - actual implementation depends on your Spring-OSGi bridge
            logger.debug("CatalogService not found in OSGi registry. " +
                        "Spring Boot integration may be required.");
            
            return null;
            
        } catch (Exception e) {
            logger.warn("Error looking up CatalogService from Spring: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gets the registered CatalogService instance.
     * 
     * @return The CatalogService instance or null if not registered
     */
    public CatalogService getCatalogService() {
        return catalogService;
    }

    /**
     * Gets the bundle context.
     * 
     * @return The bundle context
     */
    public BundleContext getBundleContext() {
        return bundleContext;
    }
}

