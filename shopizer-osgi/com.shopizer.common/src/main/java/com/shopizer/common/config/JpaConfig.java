package com.shopizer.common.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * JPA Configuration class for creating EntityManagerFactory
 * Uses DatabaseConfigService to load configuration
 */
public class JpaConfig {

    private static final String PERSISTENCE_UNIT_NAME = "shopizer-pu";
    private EntityManagerFactory entityManagerFactory;
    private final DatabaseConfigService configService;

    public JpaConfig(DatabaseConfigService configService) {
        this.configService = configService;
    }

    /**
     * Create and configure EntityManagerFactory
     * @return Configured EntityManagerFactory
     */
    public EntityManagerFactory createEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            Properties jpaProperties = configService.getJpaProperties();

            // Convert Properties to Map for Persistence.createEntityManagerFactory
            Map<String, Object> configOverrides = new HashMap<>();
            for (String key : jpaProperties.stringPropertyNames()) {
                configOverrides.put(key, jpaProperties.getProperty(key));
            }

            System.out.println("Creating EntityManagerFactory with persistence unit: " + PERSISTENCE_UNIT_NAME);
            System.out.println("JDBC URL: " + configService.getJdbcUrl());

            try {
                entityManagerFactory = Persistence.createEntityManagerFactory(
                    PERSISTENCE_UNIT_NAME,
                    configOverrides
                );
                System.out.println("EntityManagerFactory created successfully");
            } catch (Exception e) {
                System.err.println("Failed to create EntityManagerFactory: " + e.getMessage());
                throw new RuntimeException("Failed to initialize JPA", e);
            }
        }
        return entityManagerFactory;
    }

    /**
     * Get existing EntityManagerFactory
     * @return EntityManagerFactory instance
     */
    public EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            return createEntityManagerFactory();
        }
        return entityManagerFactory;
    }

    /**
     * Close EntityManagerFactory
     */
    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            System.out.println("Closing EntityManagerFactory");
            entityManagerFactory.close();
        }
    }

    /**
     * Check if EntityManagerFactory is open
     * @return true if open
     */
    public boolean isOpen() {
        return entityManagerFactory != null && entityManagerFactory.isOpen();
    }
}
