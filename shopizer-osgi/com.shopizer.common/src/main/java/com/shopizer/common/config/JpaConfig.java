package com.shopizer.common.config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.SharedCacheMode;
import jakarta.persistence.ValidationMode;
import jakarta.persistence.spi.ClassTransformer;
import jakarta.persistence.spi.PersistenceUnitInfo;
import jakarta.persistence.spi.PersistenceUnitTransactionType;
import org.hibernate.jpa.HibernatePersistenceProvider;

import javax.sql.DataSource;
import java.net.URL;
import java.util.*;

/**
 * JPA Configuration class for creating EntityManagerFactory
 * Uses programmatic configuration for OSGI compatibility
 */
public class JpaConfig {

    private EntityManagerFactory entityManagerFactory;
    private final DatabaseConfigService configService;

    public JpaConfig(DatabaseConfigService configService) {
        this.configService = configService;
    }

    /**
     * Create and configure EntityManagerFactory programmatically
     * This approach works in OSGI environments where standard persistence.xml discovery fails
     */
    public EntityManagerFactory createEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            System.out.println("Creating EntityManagerFactory programmatically for OSGI");
            System.out.println("JDBC URL: " + configService.getJdbcUrl());

            // Store the current context classloader
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();

            try {
                // Set the context classloader to this class's classloader to avoid OSGI classloader issues
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                // Create custom PersistenceUnitInfo
                PersistenceUnitInfo persistenceUnitInfo = new CustomPersistenceUnitInfo(
                    "shopizer-pu",
                    getEntityClasses(),
                    configService.getJpaProperties()
                );

                // Use Hibernate's provider directly
                HibernatePersistenceProvider provider = new HibernatePersistenceProvider();

                // Create EntityManagerFactory with the custom persistence unit
                Map<String, Object> properties = new HashMap<>();
                Properties jpaProps = configService.getJpaProperties();
                for (String key : jpaProps.stringPropertyNames()) {
                    properties.put(key, jpaProps.getProperty(key));
                }
                
                // Disable Bean Validation (not available in OSGI)
                properties.put("jakarta.persistence.validation.mode", "none");

                entityManagerFactory = provider.createContainerEntityManagerFactory(
                    persistenceUnitInfo,
                    properties
                );

                System.out.println("EntityManagerFactory created successfully");
            } catch (Exception e) {
                System.err.println("Failed to create EntityManagerFactory: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to initialize JPA", e);
            } finally {
                // Restore the original context classloader
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        }
        return entityManagerFactory;
    }

    /**
     * Get list of entity classes to register
     */
    private List<String> getEntityClasses() {
        return Arrays.asList(
            "com.shopizer.common.entity.Product",
            "com.shopizer.common.entity.Category",
            "com.shopizer.common.entity.Customer",
            "com.shopizer.common.entity.Address",
            "com.shopizer.common.entity.Cart",
            "com.shopizer.common.entity.CartItem",
            "com.shopizer.common.entity.Order",
            "com.shopizer.common.entity.OrderItem",
            "com.shopizer.common.entity.Payment",
            "com.shopizer.common.entity.ShippingOption",
            "com.shopizer.common.entity.Merchant",
            "com.shopizer.common.entity.MerchantStore"
        );
    }

    public EntityManagerFactory getEntityManagerFactory() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            return createEntityManagerFactory();
        }
        return entityManagerFactory;
    }

    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            System.out.println("Closing EntityManagerFactory");
            entityManagerFactory.close();
        }
    }

    public boolean isOpen() {
        return entityManagerFactory != null && entityManagerFactory.isOpen();
    }

    /**
     * Custom PersistenceUnitInfo implementation for programmatic JPA configuration
     */
    private static class CustomPersistenceUnitInfo implements PersistenceUnitInfo {
        private final String persistenceUnitName;
        private final List<String> managedClassNames;
        private final Properties properties;

        public CustomPersistenceUnitInfo(String persistenceUnitName, List<String> managedClassNames, Properties properties) {
            this.persistenceUnitName = persistenceUnitName;
            this.managedClassNames = managedClassNames;
            this.properties = properties;
        }

        @Override
        public String getPersistenceUnitName() {
            return persistenceUnitName;
        }

        @Override
        public String getPersistenceProviderClassName() {
            return "org.hibernate.jpa.HibernatePersistenceProvider";
        }

        @Override
        public PersistenceUnitTransactionType getTransactionType() {
            return PersistenceUnitTransactionType.RESOURCE_LOCAL;
        }

        @Override
        public DataSource getJtaDataSource() {
            return null;
        }

        @Override
        public DataSource getNonJtaDataSource() {
            return null;
        }

        @Override
        public List<String> getMappingFileNames() {
            return Collections.emptyList();
        }

        @Override
        public List<URL> getJarFileUrls() {
            return Collections.emptyList();
        }

        @Override
        public URL getPersistenceUnitRootUrl() {
            return null;
        }

        @Override
        public List<String> getManagedClassNames() {
            return managedClassNames;
        }

        @Override
        public boolean excludeUnlistedClasses() {
            return false;
        }

        @Override
        public SharedCacheMode getSharedCacheMode() {
            return SharedCacheMode.UNSPECIFIED;
        }

        @Override
        public ValidationMode getValidationMode() {
            return ValidationMode.AUTO;
        }

        @Override
        public Properties getProperties() {
            return properties;
        }

        @Override
        public String getPersistenceXMLSchemaVersion() {
            return "3.0";
        }

        @Override
        public ClassLoader getClassLoader() {
            return JpaConfig.class.getClassLoader();
        }

        @Override
        public void addTransformer(ClassTransformer transformer) {
            // Not needed for OSGI
        }

        @Override
        public ClassLoader getNewTempClassLoader() {
            return null;
        }
    }
}
