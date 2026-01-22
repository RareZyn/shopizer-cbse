package com.shopizer.merchant.repository;

import com.shopizer.common.entity.MerchantStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

/**
 * Manual JPA implementation for MerchantStore repository in OSGi.
 */
public class MerchantStoreRepositoryImpl implements MerchantStoreRepository {

    private final EntityManagerFactory entityManagerFactory;

    public MerchantStoreRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    private EntityManager em() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public MerchantStore save(MerchantStore store) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            if (store.getId() == null) {
                em.persist(store);
            } else {
                store = em.merge(store);
            }
            em.getTransaction().commit();
            return store;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error saving merchant store", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<MerchantStore> findById(Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(MerchantStore.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<MerchantStore> findByMerchantId(Long merchantId) {
        EntityManager em = em();
        try {
            TypedQuery<MerchantStore> q = em.createQuery(
                "SELECT s FROM MerchantStore s WHERE s.merchant.id = :merchantId", MerchantStore.class);
            q.setParameter("merchantId", merchantId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public List<MerchantStore> findByMerchantIdList(Long merchantId) {
        return findByMerchantId(merchantId);
    }

    public Optional<MerchantStore> findByIdAndMerchantId(Long id, Long merchantId) {
        EntityManager em = em();
        try {
            TypedQuery<MerchantStore> q = em.createQuery(
                "SELECT s FROM MerchantStore s WHERE s.id = :id AND s.merchant.id = :merchantId", MerchantStore.class);
            q.setParameter("id", id);
            q.setParameter("merchantId", merchantId);
            List<MerchantStore> results = q.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public List<MerchantStore> findAll() {
        EntityManager em = em();
        try {
            return em.createQuery("SELECT s FROM MerchantStore s", MerchantStore.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = em();
        try {
            return em.createQuery("SELECT COUNT(s) FROM MerchantStore s", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            MerchantStore store = em.find(MerchantStore.class, id);
            if (store != null) {
                em.remove(store);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting merchant store", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(MerchantStore entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteAll() {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM MerchantStore").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting merchant stores", e);
        } finally {
            em.close();
        }
    }

    // Unsupported operations required by JpaRepository
    @Override
    public void flush() { /* no-op */ }

    @Override
    public <S extends MerchantStore> S saveAndFlush(S entity) { return (S) save(entity); }

    @Override
    public <S extends MerchantStore> List<S> saveAllAndFlush(Iterable<S> entities) { throw new UnsupportedOperationException(); }

    @Override
    public void deleteAllInBatch(Iterable<MerchantStore> entities) { throw new UnsupportedOperationException(); }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> longs) { throw new UnsupportedOperationException(); }

    @Override
    public void deleteAllInBatch() { throw new UnsupportedOperationException(); }

    @Override
    public MerchantStore getOne(Long aLong) { return findById(aLong).orElse(null); }

    @Override
    public MerchantStore getById(Long aLong) { return findById(aLong).orElse(null); }

    @Override
    public MerchantStore getReferenceById(Long aLong) { return getById(aLong); }

    @Override
    public <S extends MerchantStore> List<S> findAll(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }

    @Override
    public <S extends MerchantStore> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }

    @Override
    public <S extends MerchantStore> List<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException(); }

    @Override
    public List<MerchantStore> findAllById(Iterable<Long> ids) { throw new UnsupportedOperationException(); }

    @Override
    public List<MerchantStore> findAll(org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }

    @Override
    public org.springframework.data.domain.Page<MerchantStore> findAll(org.springframework.data.domain.Pageable pageable) { throw new UnsupportedOperationException(); }

    @Override
    public <S extends MerchantStore> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }

    @Override
    public <S extends MerchantStore> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { throw new UnsupportedOperationException(); }

    @Override
    public <S extends MerchantStore> long count(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }

    @Override
    public <S extends MerchantStore> boolean exists(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }

    @Override
    public <S extends MerchantStore, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw new UnsupportedOperationException(); }

    @Override
    public void deleteAll(Iterable<? extends MerchantStore> entities) { throw new UnsupportedOperationException(); }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) { throw new UnsupportedOperationException(); }
}