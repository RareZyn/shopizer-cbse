package com.shopizer.catalog.repository;

import com.shopizer.common.entity.MerchantStore;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Manual JPA Implementation of MerchantStoreRepository
 *
 * WHY MANUAL IMPLEMENTATION?
 * Spring Data JPA's auto-implementation doesn't work in OSGi without Apache Aries JPA.
 * This class manually implements repository methods using EntityManager.
 *
 * ROLE IN ARCHITECTURE:
 * - REPOSITORY LAYER (Data Access)
 * - Manual JPA implementation for OSGi compatibility
 * - Used by CatalogServiceImpl to fetch store entities
 * - Injected via CatalogActivator from OSGi EntityManagerFactory
 */
@SuppressWarnings({"null", "NullableProblems"})
public class MerchantStoreRepositoryImpl implements MerchantStoreRepository {

    private static final Logger logger = LoggerFactory.getLogger(MerchantStoreRepositoryImpl.class);

    private final EntityManager entityManager;

    /**
     * Constructor for manual repository
     * @param entityManager Injected from OSGi EntityManagerFactory service
     */
    public MerchantStoreRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        logger.info("MerchantStoreRepositoryImpl initialized with EntityManager");
    }

    @Override
    public Optional<MerchantStore> findById(Long id) {
        try {
            MerchantStore store = entityManager.find(MerchantStore.class, id);
            return Optional.ofNullable(store);
        } catch (Exception e) {
            logger.error("Error finding store by id {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to find store by id: " + id, e);
        }
    }

    // Note: Other JpaRepository methods are not needed for catalog service
    // Only findById is required to fetch store entities when creating products
}
