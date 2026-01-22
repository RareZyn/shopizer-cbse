package com.shopizer.cart.repository;

import com.shopizer.common.entity.Cart;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manual JPA Implementation of CartRepository
 *
 * WHY MANUAL IMPLEMENTATION?
 * Spring Data JPA's auto-implementation doesn't work in OSGi without Apache Aries JPA.
 * This class manually implements all repository methods using EntityManager.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: Add items to cart
 * - FR-007: View cart contents
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
public class CartRepositoryImpl implements CartRepository {

    private static final Logger logger = LoggerFactory.getLogger(CartRepositoryImpl.class);

    private final EntityManager entityManager;

    /**
     * Constructor for manual repository
     * @param entityManager Injected from OSGi EntityManagerFactory service
     */
    public CartRepositoryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        logger.info("CartRepositoryImpl initialized with EntityManager");
    }

    // ========== Spring Data JPA Standard Methods (Manual Implementation) ==========

    @Override
    public Cart save(Cart cart) {
        try {
            if (cart.getId() == null) {
                // New entity - persist
                entityManager.getTransaction().begin();
                entityManager.persist(cart);
                entityManager.getTransaction().commit();
                logger.debug("Cart persisted with ID: {}", cart.getId());
                return cart;
            } else {
                // Existing entity - merge
                entityManager.getTransaction().begin();
                Cart merged = entityManager.merge(cart);
                entityManager.getTransaction().commit();
                logger.debug("Cart merged with ID: {}", merged.getId());
                return merged;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving cart: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save cart", e);
        }
    }

    @Override
    public <S extends Cart> List<S> saveAll(Iterable<S> entities) {
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
            logger.debug("Saved {} carts", result.size());
            return result;
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving carts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save carts", e);
        }
    }

    @Override
    public <S extends Cart> List<S> saveAllAndFlush(Iterable<S> entities) {
        try {
            List<S> result = saveAll(entities);
            entityManager.flush();
            logger.debug("Saved and flushed {} carts", result.size());
            return result;
        } catch (Exception e) {
            logger.error("Error saving and flushing carts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save and flush carts", e);
        }
    }

    @Override
    public <S extends Cart> S saveAndFlush(S entity) {
        try {
            if (entity.getId() == null) {
                entityManager.getTransaction().begin();
                entityManager.persist(entity);
                entityManager.flush();
                entityManager.getTransaction().commit();
                logger.debug("Saved and flushed cart with ID: {}", entity.getId());
                return entity;
            } else {
                entityManager.getTransaction().begin();
                S merged = (S) entityManager.merge(entity);
                entityManager.flush();
                entityManager.getTransaction().commit();
                logger.debug("Saved and flushed cart with ID: {}", merged.getId());
                return merged;
            }
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error saving and flushing cart: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save and flush cart", e);
        }
    }

    @Override
    public Optional<Cart> findById(Long id) {
        try {
            Cart cart = entityManager.find(Cart.class, id);
            return Optional.ofNullable(cart);
        } catch (Exception e) {
            logger.error("Error finding cart by ID {}: {}", id, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public Cart getReferenceById(Long id) {
        try {
            Cart reference = entityManager.getReference(Cart.class, id);
            logger.debug("Retrieved reference for cart ID: {}", id);
            return reference;
        } catch (Exception e) {
            logger.error("Error getting reference for cart ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to get cart reference", e);
        }
    }

    @Override
    @Deprecated
    public Cart getById(Long id) {
        return getReferenceById(id);
    }

    @Override
    @Deprecated
    public Cart getOne(Long id) {
        return getReferenceById(id);
    }

    @Override
    public List<Cart> findAll() {
        try {
            TypedQuery<Cart> query = entityManager.createQuery(
                "SELECT c FROM Cart c ORDER BY c.createdAt DESC",
                Cart.class
            );
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding all carts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve carts", e);
        }
    }

    @Override
    public List<Cart> findAllById(Iterable<Long> ids) {
        try {
            List<Cart> results = new ArrayList<>();
            for (Long id : ids) {
                Optional<Cart> cart = findById(id);
                cart.ifPresent(results::add);
            }
            logger.debug("Found {} carts by IDs", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding carts by IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve carts by IDs", e);
        }
    }

    @Override
    public List<Cart> findAll(org.springframework.data.domain.Sort sort) {
        try {
            StringBuilder jpql = new StringBuilder("SELECT c FROM Cart c");

            if (sort != null && sort.isSorted()) {
                jpql.append(" ORDER BY");
                sort.forEach(order -> {
                    jpql.append(" c.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<Cart> query = entityManager.createQuery(jpql.toString(), Cart.class);
            List<Cart> results = query.getResultList();
            logger.debug("Found {} carts with sorting", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error finding all carts with sort: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve carts with sort", e);
        }
    }

    @Override
    public org.springframework.data.domain.Page<Cart> findAll(org.springframework.data.domain.Pageable pageable) {
        try {
            TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(c) FROM Cart c",
                Long.class
            );
            long total = countQuery.getSingleResult();

            StringBuilder jpql = new StringBuilder("SELECT c FROM Cart c");

            if (pageable.getSort().isSorted()) {
                jpql.append(" ORDER BY");
                pageable.getSort().forEach(order -> {
                    jpql.append(" c.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<Cart> query = entityManager.createQuery(jpql.toString(), Cart.class);
            query.setFirstResult((int) pageable.getOffset());
            query.setMaxResults(pageable.getPageSize());

            List<Cart> content = query.getResultList();

            logger.debug("Found {} carts (page {} of {})", content.size(),
                        pageable.getPageNumber(), (total + pageable.getPageSize() - 1) / pageable.getPageSize());

            return new org.springframework.data.domain.PageImpl<>(content, pageable, total);
        } catch (Exception e) {
            logger.error("Error finding carts with pagination: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to retrieve carts with pagination", e);
        }
    }

    @Override
    public void delete(Cart cart) {
        try {
            entityManager.getTransaction().begin();
            if (!entityManager.contains(cart)) {
                cart = entityManager.merge(cart);
            }
            entityManager.remove(cart);
            entityManager.getTransaction().commit();
            logger.debug("Cart deleted with ID: {}", cart.getId());
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting cart: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart", e);
        }
    }

    @Override
    public void deleteById(Long id) {
        try {
            Optional<Cart> cart = findById(id);
            if (cart.isPresent()) {
                delete(cart.get());
            } else {
                logger.warn("Attempted to delete non-existent cart with ID: {}", id);
            }
        } catch (Exception e) {
            logger.error("Error deleting cart by ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart", e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            List<Cart> all = findAll();
            for (Cart cart : all) {
                delete(cart);
            }
            logger.info("All carts deleted");
        } catch (Exception e) {
            logger.error("Error deleting all carts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete all carts", e);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Cart> entities) {
        try {
            for (Cart cart : entities) {
                delete(cart);
            }
            logger.debug("Deleted multiple carts");
        } catch (Exception e) {
            logger.error("Error deleting carts: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete carts", e);
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        try {
            for (Long id : ids) {
                deleteById(id);
            }
            logger.debug("Deleted carts by IDs");
        } catch (Exception e) {
            logger.error("Error deleting carts by IDs: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete carts by IDs", e);
        }
    }

    @Override
    public void deleteAllInBatch() {
        try {
            entityManager.getTransaction().begin();
            entityManager.createQuery("DELETE FROM Cart c").executeUpdate();
            entityManager.getTransaction().commit();
            logger.info("All carts deleted in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting all carts in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete all carts in batch", e);
        }
    }

    @Override
    public void deleteAllInBatch(Iterable<Cart> entities) {
        try {
            if (!entities.iterator().hasNext()) {
                return;
            }

            entityManager.getTransaction().begin();
            for (Cart cart : entities) {
                if (cart.getId() != null) {
                    entityManager.createQuery("DELETE FROM Cart c WHERE c.id = :id")
                        .setParameter("id", cart.getId())
                        .executeUpdate();
                }
            }
            entityManager.getTransaction().commit();
            logger.debug("Carts deleted in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting carts in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete carts in batch", e);
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
                entityManager.createQuery("DELETE FROM Cart c WHERE c.id = :id")
                    .setParameter("id", id)
                    .executeUpdate();
            }
            entityManager.getTransaction().commit();
            logger.debug("Carts deleted by IDs in batch");
        } catch (Exception e) {
            if (entityManager.getTransaction().isActive()) {
                entityManager.getTransaction().rollback();
            }
            logger.error("Error deleting carts by IDs in batch: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to delete carts by IDs in batch", e);
        }
    }

    @Override
    public long count() {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(c) FROM Cart c",
                Long.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            logger.error("Error counting carts: {}", e.getMessage(), e);
            return 0L;
        }
    }

    @Override
    public boolean existsById(Long id) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                "SELECT COUNT(c) FROM Cart c WHERE c.id = :id",
                Long.class
            );
            query.setParameter("id", id);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            logger.error("Error checking cart existence for ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    // ========== Custom Query Methods ==========

    /**
     * Find cart by customer ID
     * Used for retrieving customer's shopping cart
     */
    @Override
    public Optional<Cart> findByCustomerId(Long customerId) {
        try {
            TypedQuery<Cart> query = entityManager.createQuery(
                "SELECT c FROM Cart c WHERE c.customer.id = :customerId",
                Cart.class
            );
            query.setParameter("customerId", customerId);
            List<Cart> results = query.getResultList();
            if (results.isEmpty()) {
                logger.debug("No cart found for customer ID: {}", customerId);
                return Optional.empty();
            }
            logger.debug("Found cart for customer ID: {}", customerId);
            return Optional.of(results.get(0));
        } catch (Exception e) {
            logger.error("Error finding cart by customer ID {}: {}", customerId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Delete cart by customer ID
     * Used for clearing customer's shopping cart
     */
    @Override
    public void deleteByCustomerId(Long customerId) {
        try {
            Optional<Cart> cart = findByCustomerId(customerId);
            if (cart.isPresent()) {
                delete(cart.get());
                logger.debug("Deleted cart for customer ID: {}", customerId);
            } else {
                logger.warn("No cart found to delete for customer ID: {}", customerId);
            }
        } catch (Exception e) {
            logger.error("Error deleting cart by customer ID {}: {}", customerId, e.getMessage(), e);
            throw new RuntimeException("Failed to delete cart by customer ID", e);
        }
    }

    // ========== Spring Data JPA Query By Example Methods ==========

    @Override
    public <S extends Cart> List<S> findAll(org.springframework.data.domain.Example<S> example,
                                            org.springframework.data.domain.Sort sort) {
        logger.warn("findAll(Example, Sort) called - returning all carts with sort only. " +
                   "Example matching not fully implemented.");

        try {
            S probe = example.getProbe();
            StringBuilder jpql = new StringBuilder("SELECT c FROM Cart c");

            boolean hasWhere = false;
            if (probe.getCustomer() != null && probe.getCustomer().getId() != null) {
                jpql.append(" WHERE c.customer.id = :customerId");
                hasWhere = true;
            }

            if (sort != null && sort.isSorted()) {
                jpql.append(" ORDER BY");
                sort.forEach(order -> {
                    jpql.append(" c.").append(order.getProperty());
                    jpql.append(order.isAscending() ? " ASC," : " DESC,");
                });
                jpql.setLength(jpql.length() - 1);
            }

            TypedQuery<Cart> query = entityManager.createQuery(jpql.toString(), Cart.class);

            if (probe.getCustomer() != null && probe.getCustomer().getId() != null) {
                query.setParameter("customerId", probe.getCustomer().getId());
            }

            List<S> results = (List<S>) query.getResultList();
            logger.debug("Found {} carts matching example with sort", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error in findAll(Example, Sort): {}", e.getMessage(), e);
            return (List<S>) findAll();
        }
    }

    @Override
    public <S extends Cart> List<S> findAll(org.springframework.data.domain.Example<S> example) {
        logger.warn("findAll(Example) called - returning all carts. " +
                   "Example matching not fully implemented.");

        try {
            S probe = example.getProbe();
            StringBuilder jpql = new StringBuilder("SELECT c FROM Cart c");

            if (probe.getCustomer() != null && probe.getCustomer().getId() != null) {
                jpql.append(" WHERE c.customer.id = :customerId");
            }

            TypedQuery<Cart> query = entityManager.createQuery(jpql.toString(), Cart.class);

            if (probe.getCustomer() != null && probe.getCustomer().getId() != null) {
                query.setParameter("customerId", probe.getCustomer().getId());
            }

            List<S> results = (List<S>) query.getResultList();
            logger.debug("Found {} carts matching example", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error in findAll(Example): {}", e.getMessage(), e);
            return (List<S>) findAll();
        }
    }

    @Override
    public <S extends Cart> Optional<S> findOne(org.springframework.data.domain.Example<S> example) {
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
    public <S extends Cart> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example,
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
    public <S extends Cart> long count(org.springframework.data.domain.Example<S> example) {
        try {
            List<S> results = findAll(example);
            return results.size();
        } catch (Exception e) {
            logger.error("Error counting with example: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public <S extends Cart> boolean exists(org.springframework.data.domain.Example<S> example) {
        try {
            List<S> results = findAll(example);
            return !results.isEmpty();
        } catch (Exception e) {
            logger.error("Error checking existence with example: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public <S extends Cart, R> R findBy(org.springframework.data.domain.Example<S> example,
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
