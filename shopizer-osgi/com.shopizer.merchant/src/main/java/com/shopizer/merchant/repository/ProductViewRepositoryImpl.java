package com.shopizer.merchant.repository;

import com.shopizer.common.entity.ProductView;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * FR-018: Product View Repository Implementation
 * Manual JPA implementation for OSGi environment
 */
public class ProductViewRepositoryImpl implements ProductViewRepository {

    private final EntityManagerFactory emf;

    public ProductViewRepositoryImpl(EntityManagerFactory emf) {
        this.emf = emf;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    @Override
    public long countByProductIdAndViewedAtBetween(Long productId, LocalDateTime start, LocalDateTime end) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(pv) FROM ProductView pv WHERE pv.product.id = :productId AND pv.viewedAt BETWEEN :start AND :end",
                Long.class
            );
            query.setParameter("productId", productId);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public List<ProductView> findByProductIdAndViewedAtBetween(Long productId, LocalDateTime start, LocalDateTime end) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<ProductView> query = em.createQuery(
                "SELECT pv FROM ProductView pv WHERE pv.product.id = :productId AND pv.viewedAt BETWEEN :start AND :end ORDER BY pv.viewedAt DESC",
                ProductView.class
            );
            query.setParameter("productId", productId);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long countByProductId(Long productId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(pv) FROM ProductView pv WHERE pv.product.id = :productId",
                Long.class
            );
            query.setParameter("productId", productId);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public List<ProductView> findByStoreIdAndDateRange(Long storeId, LocalDateTime start, LocalDateTime end) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<ProductView> query = em.createQuery(
                "SELECT pv FROM ProductView pv WHERE pv.store.id = :storeId AND pv.viewedAt >= :start AND pv.viewedAt <= :end",
                ProductView.class
            );
            query.setParameter("storeId", storeId);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    // Standard JpaRepository methods
    @Override
    public @NonNull <S extends ProductView> S save(@NonNull S entity) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            if (entity.getId() == null) {
                em.persist(entity);
            } else {
                entity = em.merge(entity);
            }
            em.getTransaction().commit();
            return entity;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to save ProductView", e);
        } finally {
            em.close();
        }
    }

    @Override
    public @NonNull Optional<ProductView> findById(@NonNull Long id) {
        EntityManager em = getEntityManager();
        try {
            ProductView view = em.find(ProductView.class, id);
            return Optional.ofNullable(view);
        } finally {
            em.close();
        }
    }

    @Override
    public @NonNull List<ProductView> findAll() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT pv FROM ProductView pv", ProductView.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(@NonNull Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            ProductView view = em.find(ProductView.class, id);
            if (view != null) {
                em.remove(view);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Failed to delete ProductView", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(@NonNull ProductView entity) {
        deleteById(entity.getId());
    }

    @Override
    public long count() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(pv) FROM ProductView pv", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return findById(id).isPresent();
    }

    // Unimplemented methods (not needed for FR-018)
    @Override
    public @NonNull <S extends ProductView> List<S> saveAll(@NonNull Iterable<S> entities) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull List<ProductView> findAllById(@NonNull Iterable<Long> ids) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends Long> ids) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends ProductView> entities) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteAll() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void flush() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull <S extends ProductView> S saveAndFlush(@NonNull S entity) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull <S extends ProductView> List<S> saveAllAndFlush(@NonNull Iterable<S> entities) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteAllInBatch(@NonNull Iterable<ProductView> entities) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteAllByIdInBatch(@NonNull Iterable<Long> ids) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull ProductView getOne(@NonNull Long id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull ProductView getById(@NonNull Long id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull ProductView getReferenceById(@NonNull Long id) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull <S extends ProductView> Optional<S> findOne(@NonNull Example<S> example) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull <S extends ProductView> List<S> findAll(@NonNull Example<S> example) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull <S extends ProductView> List<S> findAll(@NonNull Example<S> example, @NonNull Sort sort) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull <S extends ProductView> Page<S> findAll(@NonNull Example<S> example, @NonNull Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <S extends ProductView> long count(@NonNull Example<S> example) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public <S extends ProductView> boolean exists(@NonNull Example<S> example) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull <S extends ProductView, R> R findBy(@NonNull Example<S> example, @NonNull Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull List<ProductView> findAll(@NonNull Sort sort) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public @NonNull Page<ProductView> findAll(@NonNull Pageable pageable) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
