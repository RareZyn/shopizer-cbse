package com.shopizer.catalog.repository;

import com.shopizer.common.entity.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
@SuppressWarnings({"null", "NullableProblems", "unchecked"})
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
    public <S extends Product> List<S> saveAll(Iterable<S> entities) {
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
            logger.debug("Saved {} products", result.size());
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save products", e);
        }
    }

    @Override
    public <S extends Product> List<S> saveAllAndFlush(Iterable<S> entities) {
        try {
            List<S> result = saveAll(entities);
            entityManager.flush();
            logger.debug("Saved and flushed {} products", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error saving and flushing products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save and flush products", e);
        }
    }

    @Override
    public <S extends Product> S saveAndFlush(S entity) {
        try {
            if (entity.getId() == null) {
                entityManager.getTransaction().begin();
                entityManager.persist(entity);
                entityManager.flush();
                entityManager.getTransaction().commit();
                logger.debug("Saved and flushed product with ID: {}", entity.getId());
                return entity;
            } else {
                entityManager.getTransaction().begin();
                S merged = (S) entityManager.merge(entity);
                entityManager.flush();
                entityManager.getTransaction().commit();
                logger.debug("Saved and flushed product with ID: {}", merged.getId());
                return merged;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving and flushing product: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save and flush product", e);
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
    public Product getReferenceById(Long id) {
        try {
            // Returns a lazy-loaded reference without hitting the database
            Product reference = entityManager.getReference(Product.class, id);
            logger.debug("Retrieved reference for product ID: {}", id);
            return reference;
        } catch (Exception e) {
            logger.error("Error getting reference for product ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to get product reference", e);
        }
    }

    @Override
    @Deprecated
    public Product getById(Long id) {
        // Deprecated method - delegates to getReferenceById
        // This method exists for backwards compatibility with older Spring Data JPA versions
        return getReferenceById(id);
    }

    @Override
    @Deprecated
    public Product getOne(Long id) {
        // Another deprecated method - also delegates to getReferenceById
        // This method exists for backwards compatibility with older Spring Data JPA versions
        return getReferenceById(id);
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
    public List<Product> findAllById(Iterable<Long> ids) {
        try {
            List<Product> results = new ArrayList<>();
            for (Long id : ids) {
                Optional<Product> product = findById(id);
                product.ifPresent(results::add);
            }
            logger.debug("Found {} products by IDs", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding products by IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve products by IDs", e);
        }
    }

    @Override
    public List<Product> findAll(org.springframework.data.domain.Sort sort) {
        try {
            StringBuilder jpql = new StringBuilder("SELECT p FROM Product p");

            if (sort != null && sort.isSorted()) {
                jpql.append(" ORDER BY");
                sort.forEach(order -> {
                    jpql.append(" p.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                // Remove trailing comma
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);
            List<Product> results = query.getResultList();
            logger.debug("Found {} products with sorting", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding all products with sort: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve products with sort", e);
        }
    }

    @Override
    public org.springframework.data.domain.Page<Product> findAll(org.springframework.data.domain.Pageable pageable) {
        try {
            // Build count query
            TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(p) FROM Product p",
                Long.class
            );
            long total = countQuery.getSingleResult();

            // Build data query with sorting
            StringBuilder jpql = new StringBuilder("SELECT p FROM Product p");

            if (pageable.getSort().isSorted()) {
                jpql.append(" ORDER BY");
                pageable.getSort().forEach(order -> {
                    jpql.append(" p.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                // Remove trailing comma
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<Product> content = query.getResultList();

            logger.debug("Found {} products (page {} of {})", content.size(),
                        pageable.getPageNumber(), (total + pageable.getPageSize() - 1) / pageable.getPageSize());

            return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
        } catch (Exception e) {
            logger.error("Error finding products with pagination: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve products with pagination", e);
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
    public void deleteAll() {
        try {
            List<Product> all = findAll();
            for (Product product : all) {
                delete(product);
            }
            logger.info("All products deleted");
        } catch (Exception e) {
            logger.error("Error deleting all products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete all products", e);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Product> entities) {
        try {
            for (Product product : entities) {
                delete(product);
            }
            logger.debug("Deleted multiple products");
        } catch (Exception e) {
            logger.error("Error deleting products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete products", e);
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        try {
            for (Long id : ids) {
                deleteById(id);
            }
            logger.debug("Deleted products by IDs");
        } catch (Exception e) {
            logger.error("Error deleting products by IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete products by IDs", e);
        }
    }

    @Override
    public void deleteAllInBatch() {
        try {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM Product p").executeUpdate();
            entityManager.getTransaction().commit();
            logger.info("All products deleted in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting all products in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete all products in batch", e);
        }
    }

    @Override
    public void deleteAllInBatch(Iterable<Product> entities) {
        try {
            if (!entities.iterator().hasNext()) {
                return; // Nothing to delete
            }

            entityManager.getTransaction().begin();
            for (Product product : entities) {
                if (product.getId() != null) {
                    entityManager.createQuery("DELETE FROM Product p WHERE p.id = :id")
                        .setParameter("id", product.getId())
                        .executeUpdate();
                }
            }
            entityManager.getTransaction().commit();
            logger.debug("Products deleted in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting products in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete products in batch", e);
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
                entityManager.createQuery("DELETE FROM Product p WHERE p.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            }
            entityManager.getTransaction().commit();
            logger.debug("Products deleted by IDs in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting products by IDs in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete products by IDs in batch", e);
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
     * Find product by SKU (unique identifier)
     * Used for duplicate checking and product lookup
     */
    @Override
    public Optional<Product> findBySku(String sku) {
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.sku = :sku",
                Product.class
            );
            query.setParameter("sku", sku);
            List<Product> results = query.getResultList();
            if (results.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Error finding product by SKU '{}': {}", sku, e.getMessage(), e);
            return Optional.empty();
        }
    }

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

    /**
     * FR-005: Find products with low stock
     * Returns products where stockQuantity <= reorderLevel (low_stock_threshold)
     * If reorderLevel is null, uses default threshold of 10
     */
    @Override
    public List<Product> findLowStockProducts() {
        try {
            TypedQuery<Product> query = entityManager.createQuery(
                "SELECT p FROM Product p WHERE p.stockQuantity <= COALESCE(p.reorderLevel, 10) ORDER BY p.stockQuantity ASC",
                Product.class
            );
            List<Product> results = query.getResultList();
            logger.debug("Found {} low stock products", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding low stock products: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve low stock products", e);
        }
    }

    // ========== Spring Data JPA Query By Example Methods ==========

    /**
     * Find all entities matching the example with sorting
     * Required by JpaRepository interface
     *
     * Note: This is a simplified implementation. For production use,
     * consider implementing full Example API support with ExampleMatcher.
     */
    @Override
    public <S extends Product> List<S> findAll(org.springframework.data.domain.Example<S> example,
                                                org.springframework.data.domain.Sort sort) {
        logger.warn("findAll(Example, Sort) called - returning all products with sort only. " +
                   "Example matching not fully implemented.");

        try {
            // Get the entity class from example
            S probe = example.getProbe();

            // Build query with sorting
            StringBuilder jpql = new StringBuilder("SELECT p FROM Product p");

            // Add WHERE clause based on example probe (simplified)
            boolean hasWhere = false;
            if (probe.getName() != null) {
                jpql.append(" WHERE p.name = :name");
                hasWhere = true;
            }
            if (probe.getActive() != null) {
                jpql.append(hasWhere ? " AND" : " WHERE");
                jpql.append(" p.active = :active");
                hasWhere = true;
            }

            // Add sorting
            if (sort != null && sort.isSorted()) {
                jpql.append(" ORDER BY");
                sort.forEach(order -> {
                    jpql.append(" p.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                // Remove trailing comma
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);

            // Set parameters
            if (probe.getName() != null) {
                query.setParameter("name", probe.getName());
            }
            if (probe.getActive() != null) {
                query.setParameter("active", probe.getActive());
            }

            List<S> results = (List<S>) query.getResultList();
            logger.debug("Found {} products matching example with sort", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error in findAll(Example, Sort): {}", e.getMessage(), e);
            // Fallback: return all with sort
            return (List<S>) findAll();
        }
    }

    /**
     * Find all entities matching the example (without sorting)
     * Required by JpaRepository interface
     */
    @Override
    public <S extends Product> List<S> findAll(org.springframework.data.domain.Example<S> example) {
        logger.warn("findAll(Example) called - returning all products. " +
                   "Example matching not fully implemented.");

        try {
            S probe = example.getProbe();
            StringBuilder jpql = new StringBuilder("SELECT p FROM Product p");

            // Add WHERE clause based on example probe (simplified)
            boolean hasWhere = false;
            if (probe.getName() != null) {
                jpql.append(" WHERE p.name = :name");
                hasWhere = true;
            }
            if (probe.getActive() != null) {
                jpql.append(hasWhere ? " AND" : " WHERE");
                jpql.append(" p.active = :active");
                hasWhere = true;
            }

            TypedQuery<Product> query = entityManager.createQuery(jpql.toString(), Product.class);

            // Set parameters
            if (probe.getName() != null) {
                query.setParameter("name", probe.getName());
            }
            if (probe.getActive() != null) {
                query.setParameter("active", probe.getActive());
            }

            List<S> results = (List<S>) query.getResultList();
            logger.debug("Found {} products matching example", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error in findAll(Example): {}", e.getMessage(), e);
            return (List<S>) findAll();
        }
    }

    /**
     * Find one entity matching the Example
     * Part of QueryByExampleExecutor interface
     */
    @Override
    public <S extends Product> Optional<S> findOne(org.springframework.data.domain.Example<S> example) {
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
    public <S extends Product> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example,
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
    public <S extends Product> long count(org.springframework.data.domain.Example<S> example) {
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
    public <S extends Product> boolean exists(org.springframework.data.domain.Example<S> example) {
        try {
            List<S> results = findAll(example);
            return !results.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking existence with example: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Fluent Query API - findBy with Example
     * This is an advanced query method that uses functional programming style
     */
    @Override
    public <S extends Product, R> R findBy(org.springframework.data.domain.Example<S> example,
                                            java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        logger.warn("findBy(Example, Function) called - providing simplified FluentQuery implementation");

        // Create a simplified implementation of FetchableFluentQuery
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
                    throw new UnsupportedOperationException("as() not implemented");
                }

                @Override
                public org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S> project(java.util.Collection<String> properties) {
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
                    throw new UnsupportedOperationException("scroll() not implemented");
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
