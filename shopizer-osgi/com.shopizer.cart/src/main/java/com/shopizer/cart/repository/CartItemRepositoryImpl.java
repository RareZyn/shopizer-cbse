package com.shopizer.cart.repository;

import com.shopizer.common.entity.CartItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manual JPA Implementation of CartItemRepository
 *
 * WHY MANUAL IMPLEMENTATION?
 * Spring Data JPA's auto-implementation doesn't work in OSGi without Apache Aries JPA.
 * This class manually implements all repository methods using EntityManager.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: Add items to cart
 * - FR-007: View cart contents (items)
 * - FR-008: Update/remove cart items
 * - FR-009: Cart validation for checkout
 *
 * ROLE IN ARCHITECTURE:
 * - REPOSITORY LAYER (Data Access)
 * - Manual JPA implementation for OSGi compatibility
 * - Used by CartServiceImpl
 * - Injected via CartActivator from OSGi EntityManagerFactory
 */
@SuppressWarnings({"null", "NullableProblems", "unchecked"})
public class CartItemRepositoryImpl implements CartItemRepository {

    private static final Logger logger = LoggerFactory.getLogger(CartItemRepositoryImpl.class);

    private final EntityManager entityManager;

    /**
     * Constructor for manual repository
     * @param entityManager Injected from OSGi EntityManagerFactory service
     */
    public CartItemRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        logger.info("CartItemRepositoryImpl initialized with EntityManager");
    }

    // ========== Spring Data JPA Standard Methods (Manual Implementation) ==========

    @Override
    public CartItem save(CartItem cartItem) {
        try {
            if (cartItem.getId() == null) {
                // New entity - persist
                entityManager.getTransaction().begin();
                entityManager.persist(cartItem);
                entityManager.getTransaction().commit();
                logger.debug("CartItem persisted with ID: {}", cartItem.getId());
                return cartItem;
            } else {
                // Existing entity - merge
                entityManager.getTransaction().begin();
                CartItem merged = entityManager.merge(cartItem);
                entityManager.getTransaction().commit();
                logger.debug("CartItem merged with ID: {}", merged.getId());
                return merged;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving cart item: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save cart item", e);
        }
    }

    @Override
    public <S extends CartItem> List<S> saveAll(Iterable<S> entities) {
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
            logger.debug("Saved {} cart items", result.size());
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving cart items: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save cart items", e);
        }
    }

    @Override
    public <S extends CartItem> List<S> saveAllAndFlush(Iterable<S> entities) {
        try {
            List<S> result = saveAll(entities);
            entityManager.flush();
            logger.debug("Saved and flushed {} cart items", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error saving and flushing cart items: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save and flush cart items", e);
        }
    }

    @Override
    public <S extends CartItem> S saveAndFlush(S entity) {
        try {
            if (entity.getId() == null) {
                entityManager.getTransaction().begin();
                entityManager.persist(entity);
                entityManager.flush();
                entityManager.getTransaction().commit();
                logger.debug("Saved and flushed cart item with ID: {}", entity.getId());
                return entity;
            } else {
                entityManager.getTransaction().begin();
                S merged = (S) entityManager.merge(entity);
                entityManager.flush();
                entityManager.getTransaction().commit();
                logger.debug("Saved and flushed cart item with ID: {}", merged.getId());
                return merged;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving and flushing cart item: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save and flush cart item", e);
        }
    }

    @Override
    public Optional<CartItem> findById(Long id) {
        try {
            CartItem cartItem = entityManager.find(CartItem.class, id);
            return Optional.ofNullable(cartItem);
        } catch (Exception e) {
            logger.error("Error finding cart item by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public CartItem getReferenceById(Long id) {
        try {
            CartItem reference = entityManager.getReference(CartItem.class, id);
            logger.debug("Retrieved reference for cart item ID: {}", id);
            return reference;
        } catch (Exception e) {
            logger.error("Error getting reference for cart item ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to get cart item reference", e);
        }
    }

    @Override
    @Deprecated
    public CartItem getById(Long id) {
        return getReferenceById(id);
    }

    @Override
    @Deprecated
    public CartItem getOne(Long id) {
        return getReferenceById(id);
    }

    @Override
    public List<CartItem> findAll() {
        try {
            TypedQuery<CartItem> query = entityManager.createQuery(
                "SELECT ci FROM CartItem ci ORDER BY ci.id",
                CartItem.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding all cart items: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve cart items", e);
        }
    }

    @Override
    public List<CartItem> findAllById(Iterable<Long> ids) {
        try {
            List<CartItem> results = new ArrayList<>();
            for (Long id : ids) {
                Optional<CartItem> cartItem = findById(id);
                cartItem.ifPresent(results::add);
            }
            logger.debug("Found {} cart items by IDs", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding cart items by IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve cart items by IDs", e);
        }
    }

    @Override
    public List<CartItem> findAll(org.springframework.data.domain.Sort sort) {
        try {
            StringBuilder jpql = new StringBuilder("SELECT ci FROM CartItem ci");

            if (sort != null && sort.isSorted()) {
                jpql.append(" ORDER BY");
                sort.forEach(order -> {
                    jpql.append(" ci.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<CartItem> query = entityManager.createQuery(jpql.toString(), CartItem.class);
            List<CartItem> results = query.getResultList();
            logger.debug("Found {} cart items with sorting", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding all cart items with sort: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve cart items with sort", e);
        }
    }

    @Override
    public org.springframework.data.domain.Page<CartItem> findAll(org.springframework.data.domain.Pageable pageable) {
        try {
            TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(ci) FROM CartItem ci",
                Long.class
            );
            long total = countQuery.getSingleResult();

            StringBuilder jpql = new StringBuilder("SELECT ci FROM CartItem ci");

            if (pageable.getSort().isSorted()) {
                jpql.append(" ORDER BY");
                pageable.getSort().forEach(order -> {
                    jpql.append(" ci.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<CartItem> query = entityManager.createQuery(jpql.toString(), CartItem.class);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<CartItem> content = query.getResultList();

            logger.debug("Found {} cart items (page {} of {})", content.size(),
                        pageable.getPageNumber(), (total + pageable.getPageSize() - 1) / pageable.getPageSize());

            return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
        } catch (Exception e) {
            logger.error("Error finding cart items with pagination: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve cart items with pagination", e);
        }
    }

    @Override
    public void delete(CartItem cartItem) {
        try {
            entityManager.getTransaction().begin();
            if (!entityManager.contains(cartItem)) {
                cartItem = entityManager.merge(cartItem);
            }
            entityManager.remove(cartItem);
            entityManager.getTransaction().commit();
            logger.debug("CartItem deleted with ID: {}", cartItem.getId());
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting cart item: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart item", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            Optional<CartItem> cartItem = findById(id);
            if (cartItem.isPresent()) {
                delete(cartItem.get());
            } else {
                logger.warn("Attempted to delete non-existent cart item with ID: {}", id);
            }
        } catch (Exception e) {
            logger.error("Error deleting cart item by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart item", e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            List<CartItem> all = findAll();
            for (CartItem cartItem : all) {
                delete(cartItem);
            }
            logger.info("All cart items deleted");
        } catch (Exception e) {
            logger.error("Error deleting all cart items: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete all cart items", e);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends CartItem> entities) {
        try {
            for (CartItem cartItem : entities) {
                delete(cartItem);
            }
            logger.debug("Deleted multiple cart items");
        } catch (Exception e) {
            logger.error("Error deleting cart items: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart items", e);
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        try {
            for (Long id : ids) {
                deleteById(id);
            }
            logger.debug("Deleted cart items by IDs");
        } catch (Exception e) {
            logger.error("Error deleting cart items by IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart items by IDs", e);
        }
    }

    @Override
    public void deleteAllInBatch() {
        try {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM CartItem ci").executeUpdate();
            entityManager.getTransaction().commit();
            logger.info("All cart items deleted in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting all cart items in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete all cart items in batch", e);
        }
    }

    @Override
    public void deleteAllInBatch(Iterable<CartItem> entities) {
        try {
            if (!entities.iterator().hasNext()) {
                return;
            }

            entityManager.getTransaction().begin();
            for (CartItem cartItem : entities) {
                if (cartItem.getId() != null) {
                    entityManager.createQuery("DELETE FROM CartItem ci WHERE ci.id = :id")
                        .setParameter("id", cartItem.getId())
                        .executeUpdate();
                }
            }
            entityManager.getTransaction().commit();
            logger.debug("Cart items deleted in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting cart items in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart items in batch", e);
        }
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        try {
            if (!ids.iterator().hasNext()) {
                return;
            }

            entityManager.getTransaction().begin();
            for (Long id : ids) {
                entityManager.createQuery("DELETE FROM CartItem ci WHERE ci.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            }
            entityManager.getTransaction().commit();
            logger.debug("Cart items deleted by IDs in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting cart items by IDs in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart items by IDs in batch", e);
        }
    }

    @Override
    public long count() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(ci) FROM CartItem ci",
                Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error counting cart items: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public boolean existsById(Long id) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(ci) FROM CartItem ci WHERE ci.id = :id",
                Long.class
            );
            query.setParameter("id", id);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            logger.error("Error checking cart item existence for ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    // ========== Custom Query Methods ==========

    /**
     * Find all items in a cart
     * Used for viewing cart contents
     */
    @Override
    public List<CartItem> findByCartId(Long cartId) {
        try {
            TypedQuery<CartItem> query = entityManager.createQuery(
                "SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId ORDER BY ci.id",
                CartItem.class
            );
            query.setParameter("cartId", cartId);
            List<CartItem> results = query.getResultList();
            logger.debug("Found {} items in cart ID: {}", results.size(), cartId);
            return results;
        } catch (Exception e) {
            logger.error("Error finding cart items by cart ID {}: {}", cartId, e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve cart items by cart ID", e);
        }
    }

    /**
     * Find specific item in cart by product ID
     * Used for checking if product already exists in cart
     */
    @Override
    public Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId) {
        try {
            TypedQuery<CartItem> query = entityManager.createQuery(
                "SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.product.id = :productId",
                CartItem.class
            );
            query.setParameter("cartId", cartId);
            query.setParameter("productId", productId);
            List<CartItem> results = query.getResultList();
            if (results.isEmpty()) {
                logger.debug("No cart item found for cart ID {} and product ID {}", cartId, productId);
                return Optional.empty();
            }
            logger.debug("Found cart item for cart ID {} and product ID {}", cartId, productId);
            return Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Error finding cart item by cart ID {} and product ID {}: {}", cartId, productId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Delete all items in a cart
     * Used for clearing cart contents
     */
    @Override
    public void deleteByCartId(Long cartId) {
        try {
            List<CartItem> items = findByCartId(cartId);
            if (!items.isEmpty()) {
                entityManager.getTransaction().begin();
                for (CartItem item : items) {
                    if (!entityManager.contains(item)) {
                        item = entityManager.merge(item);
                    }
                    entityManager.remove(item);
                }
                entityManager.getTransaction().commit();
                logger.debug("Deleted {} items from cart ID: {}", items.size(), cartId);
            } else {
                logger.warn("No items found to delete for cart ID: {}", cartId);
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting cart items by cart ID {}: {}", cartId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart items by cart ID", e);
        }
    }

    // ========== Spring Data JPA Query By Example Methods ==========

    @Override
    public <S extends CartItem> List<S> findAll(org.springframework.data.domain.Example<S> example,
                                                org.springframework.data.domain.Sort sort) {
        logger.warn("findAll(Example, Sort) called - returning all cart items with sort only. " +
                   "Example matching not fully implemented.");

        try {
            S probe = example.getProbe();
            StringBuilder jpql = new StringBuilder("SELECT ci FROM CartItem ci");

            boolean hasWhere = false;
            if (probe.getCart() != null && probe.getCart().getId() != null) {
                jpql.append(" WHERE ci.cart.id = :cartId");
                hasWhere = true;
            }
            if (probe.getProduct() != null && probe.getProduct().getId() != null) {
                jpql.append(hasWhere ? " AND" : " WHERE");
                jpql.append(" ci.product.id = :productId");
                hasWhere = true;
            }

            if (sort != null && sort.isSorted()) {
                jpql.append(" ORDER BY");
                sort.forEach(order -> {
                    jpql.append(" ci.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<CartItem> query = entityManager.createQuery(jpql.toString(), CartItem.class);

            if (probe.getCart() != null && probe.getCart().getId() != null) {
                query.setParameter("cartId", probe.getCart().getId());
            }
            if (probe.getProduct() != null && probe.getProduct().getId() != null) {
                query.setParameter("productId", probe.getProduct().getId());
            }

            List<S> results = (List<S>) query.getResultList();
            logger.debug("Found {} cart items matching example with sort", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error in findAll(Example, Sort): {}", e.getMessage(), e);
            return (List<S>) findAll();
        }
    }

    @Override
    public <S extends CartItem> List<S> findAll(org.springframework.data.domain.Example<S> example) {
        logger.warn("findAll(Example) called - returning all cart items. " +
                   "Example matching not fully implemented.");

        try {
            S probe = example.getProbe();
            StringBuilder jpql = new StringBuilder("SELECT ci FROM CartItem ci");

            boolean hasWhere = false;
            if (probe.getCart() != null && probe.getCart().getId() != null) {
                jpql.append(" WHERE ci.cart.id = :cartId");
                hasWhere = true;
            }
            if (probe.getProduct() != null && probe.getProduct().getId() != null) {
                jpql.append(hasWhere ? " AND" : " WHERE");
                jpql.append(" ci.product.id = :productId");
            }

            TypedQuery<CartItem> query = entityManager.createQuery(jpql.toString(), CartItem.class);

            if (probe.getCart() != null && probe.getCart().getId() != null) {
                query.setParameter("cartId", probe.getCart().getId());
            }
            if (probe.getProduct() != null && probe.getProduct().getId() != null) {
                query.setParameter("productId", probe.getProduct().getId());
            }

            List<S> results = (List<S>) query.getResultList();
            logger.debug("Found {} cart items matching example", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error in findAll(Example): {}", e.getMessage(), e);
            return (List<S>) findAll();
        }
    }

    @Override
    public <S extends CartItem> Optional<S> findOne(org.springframework.data.domain.Example<S> example) {
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

    @Override
    public <S extends CartItem> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example,
                                                                                 org.springframework.data.domain.Pageable pageable) {
        try {
            List<S> allResults = findAll(example, pageable.getSort());
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), allResults.size());
            List<S> pageContent = allResults.subList(start, end);
            return new org.springframework.data.domain.PageImpl<>(pageContent, pageable, allResults.size());
        } catch (Exception e) {
            logger.error("Error in findAll(Example, Pageable): {}", e.getMessage(), e);
            return org.springframework.data.domain.Page.empty(pageable);
        }
    }

    @Override
    public <S extends CartItem> long count(org.springframework.data.domain.Example<S> example) {
        try {
            List<S> results = findAll(example);
            return results.size();
        } catch (Exception e) {
            logger.error("Error counting with example: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public <S extends CartItem> boolean exists(org.springframework.data.domain.Example<S> example) {
        try {
            List<S> results = findAll(example);
            return !results.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking existence with example: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public <S extends CartItem, R> R findBy(org.springframework.data.domain.Example<S> example,
                                            java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        logger.warn("findBy(Example, Function) called - providing simplified FluentQuery implementation");

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

    public void flush() {
        try {
            entityManager.flush();
        } catch (Exception e) {
            logger.error("Error flushing entity manager: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to flush changes", e);
        }
    }

    public void clear() {
        try {
            entityManager.clear();
        } catch (Exception e) {
            logger.error("Error clearing entity manager: {}", e.getMessage(), e);
        }
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
