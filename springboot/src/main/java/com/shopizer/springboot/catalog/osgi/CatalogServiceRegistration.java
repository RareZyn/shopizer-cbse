package com.shopizer.springboot.catalog.osgi;

import com.shopizer.springboot.catalog.service.CatalogService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Dictionary;
import java.util.Hashtable;

/**
 * OSGi Service Registration Component
 * 
 * This component bridges Spring Boot's dependency injection with OSGi service registry.
 * When Spring Boot application context is refreshed, it registers the CatalogService
 * as an OSGi service.
 * 
 * This allows the Catalog Module to work both as:
 * 1. A Spring Boot component (using @Service, @Autowired)
 * 2. An OSGi bundle (registering services in OSGi service registry)
 * 
 * Component-Based Software Engineering (CBSE) - Spring-OSGi Bridge
 */
@Component
public class CatalogServiceRegistration implements ApplicationContextAware, 
        ApplicationListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(CatalogServiceRegistration.class);
    
    private ApplicationContext applicationContext;
    private ServiceRegistration<CatalogService> serviceRegistration;
    private BundleContext bundleContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Called when Spring application context is refreshed.
     * Registers CatalogService in OSGi service registry if OSGi is available.
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Only register once when the root application context is refreshed
        if (event.getApplicationContext().getParent() == null) {
            registerCatalogService();
        }
    }

    /**
     * Registers the CatalogService in OSGi service registry.
     */
    private void registerCatalogService() {
        try {
            // Get CatalogService from Spring context
            CatalogService catalogService = applicationContext.getBean(CatalogService.class);
            
            if (catalogService == null) {
                logger.warn("CatalogService not found in Spring application context");
                return;
            }

            // Try to get OSGi BundleContext from Spring context
            // In a full OSGi environment, this would be available
            bundleContext = getBundleContext();
            
            if (bundleContext == null) {
                logger.info("OSGi BundleContext not available. " +
                           "Catalog Module running in Spring Boot mode only.");
                logger.info("To enable OSGi mode, deploy this as an OSGi bundle.");
                return;
            }

            // Create service properties
            Dictionary<String, Object> properties = new Hashtable<>();
            properties.put("service.description", "Catalog Service - Product and Category Management");
            properties.put("service.vendor", "Shopizer CBSE");
            properties.put("module.name", "catalog");
            properties.put("functional.requirements", "FR-001,FR-002,FR-003,FR-004,FR-005");
            properties.put("spring.managed", "true");

            // Register the service
            serviceRegistration = bundleContext.registerService(
                CatalogService.class,
                catalogService,
                properties
            );

            logger.info("========================================");
            logger.info("CatalogService registered in OSGi service registry");
            logger.info("  Service Interface: {}", CatalogService.class.getName());
            logger.info("  Bundle: {}", bundleContext.getBundle().getSymbolicName());
            logger.info("  Properties: {}", properties);
            logger.info("========================================");

        } catch (Exception e) {
            logger.error("Error registering CatalogService in OSGi service registry", e);
        }
    }

    /**
     * Unregisters the CatalogService from OSGi service registry.
     */
    public void unregisterCatalogService() {
        if (serviceRegistration != null) {
            try {
                serviceRegistration.unregister();
                logger.info("CatalogService unregistered from OSGi service registry");
                serviceRegistration = null;
            } catch (Exception e) {
                logger.error("Error unregistering CatalogService", e);
            }
        }
    }

    /**
     * Attempts to get OSGi BundleContext from Spring context.
     * In a full OSGi environment, this would be provided by Spring DM or similar.
     * 
     * @return BundleContext or null if not available
     */
    private BundleContext getBundleContext() {
        try {
            // Method 1: Try to get from Spring context as a bean
            if (applicationContext.containsBean("bundleContext")) {
                return applicationContext.getBean("bundleContext", BundleContext.class);
            }

            // Method 2: Try to get from system properties or environment
            // This is a placeholder - actual implementation depends on your OSGi framework
            
            // Method 3: In Apache Felix or Eclipse Equinox, you might access it differently
            // For now, return null to indicate OSGi is not active
            
            return null;
            
        } catch (Exception e) {
            logger.debug("BundleContext not available: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gets the service registration.
     * 
     * @return ServiceRegistration or null
     */
    public ServiceRegistration<CatalogService> getServiceRegistration() {
        return serviceRegistration;
    }
}

