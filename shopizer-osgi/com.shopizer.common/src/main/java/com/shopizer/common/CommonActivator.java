package com.shopizer.common;

import com.shopizer.common.config.DatabaseConfigService;
import com.shopizer.common.config.DatabaseConfigServiceImpl;
import com.shopizer.common.config.JpaConfig;
import jakarta.persistence.EntityManagerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Activator for the Common module
 * Registers DatabaseConfigService and EntityManagerFactory as OSGI services
 */
public class CommonActivator implements BundleActivator {

    private ServiceRegistration<DatabaseConfigService> configServiceRegistration;
    private ServiceRegistration<EntityManagerFactory> emfServiceRegistration;
    private JpaConfig jpaConfig;

    @Override
    public void start(BundleContext context) throws Exception {
        System.out.println("=== Starting Common Module ===");

        try {
            // Create and register DatabaseConfigService
            DatabaseConfigService configService = new DatabaseConfigServiceImpl();
            configServiceRegistration = context.registerService(
                DatabaseConfigService.class,
                configService,
                null
            );
            System.out.println("✓ DatabaseConfigService registered");

            // Create JPA configuration
            jpaConfig = new JpaConfig(configService);

            // Create and register EntityManagerFactory
            EntityManagerFactory emf = jpaConfig.createEntityManagerFactory();
            emfServiceRegistration = context.registerService(
                EntityManagerFactory.class,
                emf,
                null
            );
            System.out.println("✓ EntityManagerFactory registered");

            System.out.println("=== Common Module started successfully ===");

        } catch (Exception e) {
            System.err.println("Failed to start Common Module: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("=== Stopping Common Module ===");

        try {
            // Unregister services
            if (emfServiceRegistration != null) {
                emfServiceRegistration.unregister();
                System.out.println("✓ EntityManagerFactory unregistered");
            }

            if (configServiceRegistration != null) {
                configServiceRegistration.unregister();
                System.out.println("✓ DatabaseConfigService unregistered");
            }

            // Close EntityManagerFactory
            if (jpaConfig != null) {
                jpaConfig.close();
                System.out.println("✓ EntityManagerFactory closed");
            }

            System.out.println("=== Common Module stopped ===");

        } catch (Exception e) {
            System.err.println("Error stopping Common Module: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
