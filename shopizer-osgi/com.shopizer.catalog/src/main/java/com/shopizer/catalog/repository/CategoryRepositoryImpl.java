package com.shopizer.catalog.repository;

import com.shopizer.common.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    public <S extends Category> List<S> saveAll(Iterable<S> entities) {
        try {
            List<S> result = new ArrayList<>();
            entityManager.getTransaction().begin();
            for (S entity : entities) {
                if (entity.getId() == null) {
                    entityManager.persist(entity);
                    result.add(entity);
                } else {
                    S merged = entityManager.merge(entity);
                    result.add(merged);
                }
            }
            entityManager.getTransaction().commit();
            logger.debug("Saved {} categories", result.size());
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save categories", e);
        }
    }

    @Override
    public <S extends Category> List<S> saveAllAndFlush(Iterable<S> entities) {
        try {
            List<S> result = saveAll(entities);
            entityManager.flush();
            logger.debug("Saved and flushed {} categories", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error saving and flushing categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save and flush categories", e);
        }
    }

    @Override
    public <S extends Category> S saveAndFlush(S entity) {
        try {
            if (entity.getId() == null) {
                entityManager.getTransaction().begin();
                entityManager.persist(entity);
                entityManager.flush();
                entityManager.getTransaction().commit();
                logger.debug("Saved and flushed category with ID: {}", entity.getId());
                return entity;
            } else {
                entityManager.getTransaction().begin();
                @SuppressWarnings("unchecked")
                S merged = (S) entityManager.merge(entity);
                entityManager.flush();
                entityManager.getTransaction().commit();
                logger.debug("Saved and flushed category with ID: {}", merged.getId());
                return merged;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving and flushing category: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save and flush category", e);
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
    public Category getReferenceById(Long id) {
        try {
            // Returns a lazy-loaded reference without hitting the database
            Category reference = entityManager.getReference(Category.class, id);
            logger.debug("Retrieved reference for category ID: {}", id);
            return reference;
        } catch (Exception e) {
            logger.error("Error getting reference for category ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to get category reference", e);
        }
    }

    @Override
    @Deprecated
    public Category getById(Long id) {
        // Deprecated method - delegates to getReferenceById
        // This method exists for backwards compatibility with older Spring Data JPA versions
        return getReferenceById(id);
    }

    @Override
    @Deprecated
    public Category getOne(Long id) {
        // Another deprecated method - also delegates to getReferenceById
        // This method exists for backwards compatibility with older Spring Data JPA versions
        return getReferenceById(id);
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
    public List<Category> findAllById(Iterable<Long> ids) {
        try {
            List<Category> results = new ArrayList<>();
            for (Long id : ids) {
                Optional<Category> category = findById(id);
                category.ifPresent(results::add);
            }
            logger.debug("Found {} categories by IDs", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding categories by IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve categories by IDs", e);
        }
    }

    @Override
    public List<Category> findAll(org.springframework.data.domain.Sort sort) {
        try {
            StringBuilder jpql = new StringBuilder("SELECT c FROM Category c");

            if (sort != null && sort.isSorted()) {
                jpql.append(" ORDER BY");
                sort.forEach(order -> {
                    jpql.append(" c.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                // Remove trailing comma
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<Category> query = entityManager.createQuery(jpql.toString(), Category.class);
            List<Category> results = query.getResultList();
            logger.debug("Found {} categories with sorting", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding all categories with sort: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve categories with sort", e);
        }
    }

    @Override
    public org.springframework.data.domain.Page<Category> findAll(org.springframework.data.domain.Pageable pageable) {
        try {
            // Build count query
            TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(c) FROM Category c",
                Long.class
            );
            long total = countQuery.getSingleResult();

            // Build data query with sorting
            StringBuilder jpql = new StringBuilder("SELECT c FROM Category c");

            if (pageable.getSort().isSorted()) {
                jpql.append(" ORDER BY");
                pageable.getSort().forEach(order -> {
                    jpql.append(" c.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                // Remove trailing comma
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<Category> query = entityManager.createQuery(jpql.toString(), Category.class);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<Category> content = query.getResultList();

            logger.debug("Found {} categories (page {} of {})", content.size(),
                        pageable.getPageNumber(), (total + pageable.getPageSize() - 1) / pageable.getPageSize());

            return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
        } catch (Exception e) {
            logger.error("Error finding categories with pagination: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve categories with pagination", e);
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
    public void deleteAll() {
        try {
            List<Category> all = findAll();
            for (Category category : all) {
                delete(category);
            }
            logger.info("All categories deleted");
        } catch (Exception e) {
            logger.error("Error deleting all categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete all categories", e);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Category> entities) {
        try {
            for (Category category : entities) {
                delete(category);
            }
            logger.debug("Deleted multiple categories");
        } catch (Exception e) {
            logger.error("Error deleting categories: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete categories", e);
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        try {
            for (Long id : ids) {
                deleteById(id);
            }
            logger.debug("Deleted categories by IDs");
        } catch (Exception e) {
            logger.error("Error deleting categories by IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete categories by IDs", e);
        }
    }

    @Override
    public void deleteAllInBatch() {
        try {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM Category c").executeUpdate();
            entityManager.getTransaction().commit();
            logger.info("All categories deleted in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting all categories in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete all categories in batch", e);
        }
    }

    @Override
    public void deleteAllInBatch(Iterable<Category> entities) {
        try {
            if (!entities.iterator().hasNext()) {
                return; // Nothing to delete
            }

            entityManager.getTransaction().begin();
            for (Category category : entities) {
                if (category.getId() != null) {
                    entityManager.createQuery("DELETE FROM Category c WHERE c.id = :id")
                        .setParameter("id", category.getId())
                        .executeUpdate();
                }
            }
            entityManager.getTransaction().commit();
            logger.debug("Categories deleted in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting categories in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete categories in batch", e);
        }
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        try {
            if (!ids.iterator().hasNext()) {
                return; // Nothing to delete
            }

            entityManager.getTransaction().begin();
            for (Long id : ids) {
                entityManager.createQuery("DELETE FROM Category c WHERE c.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            }
            entityManager.getTransaction().commit();
            logger.debug("Categories deleted by IDs in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting categories by IDs in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete categories by IDs in batch", e);
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
     * Find categories with no parent (root categories)
     * Required by CategoryRepository interface
     * FR-002: Support category hierarchy
     */
    @Override
    public List<Category> findByParentIsNull() {
        try {
            TypedQuery<Category> query = entityManager.createQuery(
                "SELECT c FROM Category c WHERE c.parent IS NULL ORDER BY c.name",
                Category.class
            );
            List<Category> results = query.getResultList();
            logger.debug("Found {} root categories (via findByParentIsNull)", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding categories with null parent: {}", e.getMessage(), e);
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

    // ========== Spring Data JPA Query By Example Methods ==========

    /**
     * Find all entities matching the example (without sorting)
     * Required by JpaRepository interface
     */
    @Override
    public <S extends Category> List<S> findAll(org.springframework.data.domain.Example<S> example) {
        logger.warn("findAll(Example) called - returning all categories. " +
                   "Example matching not fully implemented.");

        try {
            S probe = example.getProbe();
            StringBuilder jpql = new StringBuilder("SELECT c FROM Category c");

            // Add WHERE clause based on example probe (simplified)
            boolean hasWhere = false;
            if (probe.getName() != null) {
                jpql.append(" WHERE c.name = :name");
                hasWhere = true;
            }

            TypedQuery<Category> query = entityManager.createQuery(jpql.toString(), Category.class);

            // Set parameters
            if (probe.getName() != null) {
                query.setParameter("name", probe.getName());
            }

            @SuppressWarnings("unchecked")
            List<S> results = (List<S>) query.getResultList();
            logger.debug("Found {} categories matching example", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error in findAll(Example): {}", e.getMessage(), e);
            return (List<S>) findAll();
        }
    }

    /**
     * Find all entities matching the example with sorting
     * Required by JpaRepository interface
     *
     * Note: This is a simplified implementation. For production use,
     * consider implementing full Example API support with ExampleMatcher.
     */
    @Override
    public <S extends Category> List<S> findAll(org.springframework.data.domain.Example<S> example,
                                                 org.springframework.data.domain.Sort sort) {
        logger.warn("findAll(Example, Sort) called - returning all categories with sort only. " +
                   "Example matching not fully implemented.");

        try {
            // Get the entity class from example
            S probe = example.getProbe();

            // Build query with sorting
            StringBuilder jpql = new StringBuilder("SELECT c FROM Category c");

            // Add WHERE clause based on example probe (simplified)
            boolean hasWhere = false;
            if (probe.getName() != null) {
                jpql.append(" WHERE c.name = :name");
                hasWhere = true;
            }

            // Add sorting
            if (sort != null && sort.isSorted()) {
                jpql.append(" ORDER BY");
                sort.forEach(order -> {
                    jpql.append(" c.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                // Remove trailing comma
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<Category> query = entityManager.createQuery(jpql.toString(), Category.class);

            // Set parameters
            if (probe.getName() != null) {
                query.setParameter("name", probe.getName());
            }

            @SuppressWarnings("unchecked")
            List<S> results = (List<S>) query.getResultList();
            logger.debug("Found {} categories matching example with sort", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error in findAll(Example, Sort): {}", e.getMessage(), e);
            // Fallback: return all with sort
            return (List<S>) findAll();
        }
    }

    /**
     * Find one entity matching the Example
     * Part of QueryByExampleExecutor interface
     */
    @Override
    public <S extends Category> Optional<S> findOne(org.springframework.data.domain.Example<S> example) {
        try {
            List<S> results = findAll(example);
            if (results.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Error finding one with example: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Find all entities matching Example with pagination
     * Part of QueryByExampleExecutor interface
     */
    @Override
    public <S extends Category> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example,
                                                                                  org.springframework.data.domain.Pageable pageable) {
        try {
            // Get all matching results with sorting
            List<S> allResults = findAll(example, pageable.getSort());

            // Apply pagination
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), allResults.size());
            List<S> pageContent = allResults.subList(start, end);

            return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, allResults.size());
        } catch (Exception e) {
            logger.error("Error in findAll(Example, Pageable): {}", e.getMessage(), e);
            return org.springframework.data.domain.Page.empty(pageable);
        }
    }

    /**
     * Count entities matching the Example
     * Part of QueryByExampleExecutor interface
     */
    @Override
    public <S extends Category> long count(org.springframework.data.domain.Example<S> example) {
        try {
            List<S> results = findAll(example);
            return results.size();
        } catch (Exception e) {
            logger.error("Error counting with example: {}", e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Check if any entities match the Example
     * Part of QueryByExampleExecutor interface
     */
    @Override
    public <S extends Category> boolean exists(org.springframework.data.domain.Example<S> example) {
        try {
            List<S> results = findAll(example);
            return !results.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking existence with example: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Fluent Query API - Find by Example with functional transformation
     * Provides simplified implementation of Spring Data's FluentQuery API
     */
    @Override
    public <S extends Category, R> R findBy(org.springframework.data.domain.Example<S> example,
                                              java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        logger.warn("findBy(Example, Function) called - providing simplified FluentQuery implementation");

        // Create anonymous implementation of FetchableFluentQuery
        org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S> fluentQuery =
            new org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>() {
                private org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.unsorted();
                private Integer limit = null;

                @Override
                public org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S> sortBy(org.springframework.data.domain.Sort sort) {
                    this.sort = sort;
                    return this;
                }

                @Override
                public org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S> limit(int limit) {
                    this.limit = limit;
                    return this;
                }

                @Override
                public <NR> org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<NR> as(Class<NR> resultType) {
                    throw new UnsupportedOperationException("as() not implemented in manual JPA repository");
                }

                @Override
                public org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S> project(java.util.Collection<String> properties) {
                    logger.warn("project() called but not fully implemented");
                    return this;
                }

                @Override
                public S oneValue() {
                    List<S> results = findAll(example, sort);
                    return results.isEmpty() ? null : results.get(0);
                }

                @Override
                public S firstValue() {
                    return oneValue();
                }

                @Override
                public List<S> all() {
                    List<S> results = findAll(example, sort);
                    if (limit != null && results.size() > limit) {
                        return new ArrayList<>(results.subList(0, limit));
                    }
                    return results;
                }

                @Override
                public org.springframework.data.domain.Page<S> page(org.springframework.data.domain.Pageable pageable) {
                    List<S> results = findAll(example, pageable.getSort());
                    int start = (int) pageable.getOffset();
                    int end = Math.min(start + pageable.getPageSize(), results.size());
                    List<S> pageContent = results.subList(start, end);
                    return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, results.size());
                }

                @Override
                public org.springframework.data.domain.Window<S> scroll(org.springframework.data.domain.ScrollPosition position) {
                    throw new UnsupportedOperationException("scroll() not implemented in manual JPA repository");
                }

                @Override
                public long count() {
                    return findAll(example).size();
                }

                @Override
                public boolean exists() {
                    return !findAll(example).isEmpty();
                }

                @Override
                public java.util.stream.Stream<S> stream() {
                    return all().stream();
                }
            };

        // Apply the user's query function and return result
        return queryFunction.apply(fluentQuery);
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
