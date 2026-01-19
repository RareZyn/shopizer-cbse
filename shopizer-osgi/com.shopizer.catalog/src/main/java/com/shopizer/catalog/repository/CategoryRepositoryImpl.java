package com.shopizer.catalog.repository;

import com.shopizer.common.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Manual JPA Implementation of CategoryRepository
 *
 * WHY MANUAL IMPLEMENTATION?
 * Spring Data JPA's auto-implementation doesn't work in OSGi without Apache Aries JPA.
 * This class manually implements all repository methods using EntityManager.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-002: Category CRUD operations
 * - FR-004: Browse products by category (requires category hierarchy)
 *
 * ROLE IN ARCHITECTURE:
 * - REPOSITORY LAYER (Data Access)
 * - Manual JPA implementation for OSGi compatibility
 * - Used by CatalogServiceImpl
 * - Injected via CatalogActivator from OSGi EntityManagerFactory
 *
 * CATEGORY HIERARCHY SUPPORT:
 * Categories can have parent-child relationships forming a tree structure.
 * This allows nested categories like: Electronics > Computers > Laptops
 */
public class CategoryRepositoryImpl implements CategoryRepository {

    private static final Logger logger = LoggerFactory.getLogger(CategoryRepositoryImpl.class);

    private final EntityManager entityManager;

    /**
     * Constructor for manual repository
     * @param entityManager Injected from OSGi EntityManagerFactory service
     */
    public CategoryRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        logger.info("CategoryRepositoryImpl initialized with EntityManager");
    }

    // ========== Spring Data JPA Standard Methods (Manual Implementation) ==========

    @Override
    public Category save(Category category) {
        try {
            if (category.getId() == null) {
                // New entity - persist
                entityManager.getTransaction().begin();
                entityManager.persist(category);
                entityManager.getTransaction().commit();
                logger.debug("Category persisted with ID: {}", category.getId());
                return category;
            } else {
                // Existing entity - merge
                entityManager.getTransaction().begin();
                Category merged = entityManager.merge(category);
                entityManager.getTransaction().commit();
                logger.debug("Category merged with ID: {}", merged.getId());
                return merged;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save category", e);
        }
    }

    @Override
    public Optional<Category> findById(Long id) {
        try {
            Category category = entityManager.find(Category.class, id);
            return Optional.ofNullable(category);
        } catch (Exception e) {
            logger.error("Error finding category by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public List<Category> findAll() {
        try {
            TypedQuery<Category> query = entityManager.createQuery(
                "SELECT c FROM Category c ORDER BY c.name",
                Category.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding all categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve categories", e);
        }
    }

    @Override
    public void delete(Category category) {
        try {
            entityManager.getTransaction().begin();
            if (!entityManager.contains(category)) {
                // Reattach if detached
                category = entityManager.merge(category);
            }
            entityManager.remove(category);
            entityManager.getTransaction().commit();
            logger.debug("Category deleted with ID: {}", category.getId());
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete category", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            Optional<Category> category = findById(id);
            if (category.isPresent()) {
                delete(category.get());
            } else {
                logger.warn("Attempted to delete non-existent category with ID: {}", id);
            }
        } catch (Exception e) {
            logger.error("Error deleting category by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete category", e);
        }
    }

    @Override
    public long count() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(c) FROM Category c",
                Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error counting categories: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public boolean existsById(Long id) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(c) FROM Category c WHERE c.id = :id",
                Long.class
            );
            query.setParameter("id", id);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            logger.error("Error checking category existence for ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    // ========== Custom Query Methods ==========

    /**
     * FR-002: Category hierarchy support
     * Find all subcategories of a parent category
     *
     * Example:
     * Parent: Electronics (ID=1)
     * Returns: [Computers, Phones, Tablets] (all direct children)
     */
    @Override
    public List<Category> findByParentId(Long parentId) {
        try {
            TypedQuery<Category> query = entityManager.createQuery(
                "SELECT c FROM Category c WHERE c.parent.id = :parentId ORDER BY c.name",
                Category.class
            );
            query.setParameter("parentId", parentId);
            List<Category> results = query.getResultList();
            logger.debug("Found {} subcategories for parent ID: {}", results.size(), parentId);
            return results;
        } catch (Exception e) {
            logger.error("Error finding subcategories for parent ID {}: {}", parentId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve subcategories", e);
        }
    }

    /**
     * Find root categories (categories with no parent)
     * Used to build category tree starting from top level
     */
    public List<Category> findRootCategories() {
        try {
            TypedQuery<Category> query = entityManager.createQuery(
                "SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name",
                Category.class
            );
            List<Category> results = query.getResultList();
            logger.debug("Found {} root categories", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding root categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve root categories", e);
        }
    }

    /**
     * Find category by name (exact match)
     * Useful for preventing duplicate category names
     */
    public Optional<Category> findByName(String name) {
        try {
            TypedQuery<Category> query = entityManager.createQuery(
                "SELECT c FROM Category c WHERE c.name = :name",
                Category.class
            );
            query.setParameter("name", name);
            List<Category> results = query.getResultList();
            if (results.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Error finding category by name '{}': {}", name, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Count subcategories of a parent
     * Used to check if category has children before deletion
     */
    public long countByParentId(Long parentId) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(c) FROM Category c WHERE c.parent.id = :parentId",
                Long.class
            );
            query.setParameter("parentId", parentId);
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error counting subcategories for parent ID {}: {}", parentId, e.getMessage(), e);
            return 0L;
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
