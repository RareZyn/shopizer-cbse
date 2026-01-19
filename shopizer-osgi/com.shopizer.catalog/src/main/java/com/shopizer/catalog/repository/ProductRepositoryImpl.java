package com.shopizer.catalog.repository;

import com.shopizer.common.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Manual JPA Implementation of ProductRepository
 *
 * WHY MANUAL IMPLEMENTATION?
 * Spring Data JPA's auto-implementation doesn't work in OSGi without Apache Aries JPA.
 * This class manually implements all repository methods using EntityManager.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-001: Product CRUD operations
 * - FR-003: Search products by keyword
 * - FR-004: Browse products by category
 * - FR-005: Track product stock levels
 *
 * ROLE IN ARCHITECTURE:
 * - REPOSITORY LAYER (Data Access)
 * - Manual JPA implementation for OSGi compatibility
 * - Used by CatalogServiceImpl
 * - Injected via CatalogActivator from OSGi EntityManagerFactory
 */
public class ProductRepositoryImpl implements ProductRepository {

    private static final Logger logger = LoggerFactory.getLogger(ProductRepositoryImpl.class);

    private final EntityManager entityManager;

    /**
     * Constructor for manual repository
     * @param entityManager Injected from OSGi EntityManagerFactory service
     */
    public ProductRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        logger.info("ProductRepositoryImpl initialized with EntityManager");
    }

    // ========== Spring Data JPA Standard Methods (Manual Implementation) ==========

    @Override
    public Product save(Product product) {
        try {
            if (product.getId() == null) {
                // New entity - persist
                entityManager.getTransaction().begin();
                entityManager.persist(product);
                entityManager.getTransaction().commit();
                logger.debug("Product persisted with ID: {}", product.getId());
                return product;
            } else {
                // Existing entity - merge
                entityManager.getTransaction().begin();
                Product merged = entityManager.merge(product);
                entityManager.getTransaction().commit();
                logger.debug("Product merged with ID: {}", merged.getId());
                return merged;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving product: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save product", e);
        }
    }

    @Override
    public Optional<Product> findById(Long id) {
        try {
            Product product = entityManager.find(Product.class, id);
            return Optional.ofNullable(product);
        } catch (Exception e) {
            logger.error("Error finding product by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findAll() {
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p ORDER BY p.createdAt DESC",
                Product.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding all products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve products", e);
        }
    }

    @Override
    public void delete(Product product) {
        try {
            entityManager.getTransaction().begin();
            if (!entityManager.contains(product)) {
                // Reattach if detached
                product = entityManager.merge(product);
            }
            entityManager.remove(product);
            entityManager.getTransaction().commit();
            logger.debug("Product deleted with ID: {}", product.getId());
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting product: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            Optional<Product> product = findById(id);
            if (product.isPresent()) {
                delete(product.get());
            } else {
                logger.warn("Attempted to delete non-existent product with ID: {}", id);
            }
        } catch (Exception e) {
            logger.error("Error deleting product by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    @Override
    public long count() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(p) FROM Product p",
                Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error counting products: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public boolean existsById(Long id) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(p) FROM Product p WHERE p.id = :id",
                Long.class
            );
            query.setParameter("id", id);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            logger.error("Error checking product existence for ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    // ========== Custom Query Methods ==========

    /**
     * FR-003: Search products by keyword
     * Case-insensitive partial match on product name
     */
    @Override
    public List<Product> findByNameContainingIgnoreCase(String keyword) {
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(:keyword) ORDER BY p.name",
                Product.class
            );
            query.setParameter("keyword", "%" + keyword + "%");
            List<Product> results = query.getResultList();
            logger.debug("Found {} products matching keyword: {}", results.size(), keyword);
            return results;
        } catch (Exception e) {
            logger.error("Error searching products by keyword '{}': {}", keyword, e.getMessage(), e);
            throw new RuntimeException("Failed to search products", e);
        }
    }

    /**
     * FR-004: Browse products by category
     * Returns all products in a specific category
     */
    @Override
    public List<Product> findByCategoryId(Long categoryId) {
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.category.id = :categoryId ORDER BY p.name",
                Product.class
            );
            query.setParameter("categoryId", categoryId);
            List<Product> results = query.getResultList();
            logger.debug("Found {} products in category ID: {}", results.size(), categoryId);
            return results;
        } catch (Exception e) {
            logger.error("Error finding products by category ID {}: {}", categoryId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve products by category", e);
        }
    }

    /**
     * Find products by store (multi-store support)
     * Used by merchants to view their products
     */
    @Override
    public List<Product> findByStoreId(Long storeId) {
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.store.id = :storeId ORDER BY p.name",
                Product.class
            );
            query.setParameter("storeId", storeId);
            List<Product> results = query.getResultList();
            logger.debug("Found {} products for store ID: {}", results.size(), storeId);
            return results;
        } catch (Exception e) {
            logger.error("Error finding products by store ID {}: {}", storeId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve products by store", e);
        }
    }

    /**
     * Find only active products
     * Used for customer-facing product listings
     */
    @Override
    public List<Product> findByActiveTrue() {
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.isActive = true ORDER BY p.name",
                Product.class
            );
            List<Product> results = query.getResultList();
            logger.debug("Found {} active products", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding active products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve active products", e);
        }
    }

    // ========== Additional Helper Methods ==========

    /**
     * Flush pending changes to database
     */
    public void flush() {
        try {
            entityManager.flush();
        } catch (Exception e) {
            logger.error("Error flushing entity manager: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to flush changes", e);
        }
    }

    /**
     * Clear persistence context
     */
    public void clear() {
        try {
            entityManager.clear();
        } catch (Exception e) {
            logger.error("Error clearing entity manager: {}", e.getMessage(), e);
        }
    }

    /**
     * Get EntityManager for advanced operations
     * Use sparingly - prefer repository methods
     */
    public EntityManager getEntityManager() {
        return entityManager;
    }
}
