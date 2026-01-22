package com.shopizer.merchant.repository;

import com.shopizer.common.entity.Merchant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Manual JPA implementation for OSGi without Spring Data runtime.
 */
public class MerchantRepositoryImpl implements MerchantRepository {

    private final EntityManagerFactory entityManagerFactory;

    public MerchantRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    private EntityManager em() {
        return entityManagerFactory.createEntityManager();
    }

    @Override

    public @NonNull <S extends Merchant> S save(@NonNull S merchant) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            if (merchant.getId() == null) {
                em.persist(merchant);
            } else {
                merchant = (S) em.merge(merchant);
            }
            em.getTransaction().commit();
            return merchant;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error saving merchant", e);
        } finally {
            em.close();
        }
    }

    @Override
    public @NonNull Optional<Merchant> findById(@NonNull Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(Merchant.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Merchant> findByEmail(String email) {
        EntityManager em = em();
        try {
            TypedQuery<Merchant> q = em.createQuery(
                "SELECT m FROM Merchant m WHERE m.email = :email", Merchant.class);
            q.setParameter("email", email);
            List<Merchant> results = q.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return findById(id).isPresent();
    }

    @Override
    public @NonNull List<Merchant> findAll() {
        EntityManager em = em();
        try {
            return em.createQuery("SELECT m FROM Merchant m", Merchant.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = em();
        try {
            return em.createQuery("SELECT COUNT(m) FROM Merchant m", Long.class).getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(@NonNull Long id) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            Merchant m = em.find(Merchant.class, id);
            if (m != null) {
                em.remove(m);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting merchant", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(@NonNull Merchant entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteAll() {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Merchant").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting merchants", e);
        } finally {
            em.close();
        }
    }

    // Unsupported operations required by JpaRepository
    @Override
    public void flush() { /* no-op */ }

    @Override

    public @NonNull <S extends Merchant> S saveAndFlush(@NonNull S entity) { return (S) save(entity); }

    @Override
    public @NonNull <S extends Merchant> List<S> saveAllAndFlush(@NonNull Iterable<S> entities) { throw new UnsupportedOperationException(); }

    @Override
    public void deleteAllInBatch(@NonNull Iterable<Merchant> entities) { throw new UnsupportedOperationException(); }

    @Override
    public void deleteAllByIdInBatch(@NonNull Iterable<Long> longs) { throw new UnsupportedOperationException(); }

    @Override
    public void deleteAllInBatch() { throw new UnsupportedOperationException(); }

    @Override
    public @NonNull Merchant getOne(@NonNull Long aLong) {
        return findById(aLong).orElseThrow(() ->
            new jakarta.persistence.EntityNotFoundException("Merchant not found with id: " + aLong));
    }

    @Override
    public @NonNull Merchant getById(@NonNull Long aLong) {
        return findById(aLong).orElseThrow(() ->
            new jakarta.persistence.EntityNotFoundException("Merchant not found with id: " + aLong));
    }

    @Override
    public @NonNull Merchant getReferenceById(@NonNull Long aLong) { return getById(aLong); }

    @Override
    public @NonNull <S extends Merchant> List<S> findAll(@NonNull org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }

    @Override
    public @NonNull <S extends Merchant> List<S> findAll(@NonNull org.springframework.data.domain.Example<S> example, @NonNull org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }

    @Override
    public @NonNull <S extends Merchant> List<S> saveAll(@NonNull Iterable<S> entities) { throw new UnsupportedOperationException(); }

    @Override
    public @NonNull List<Merchant> findAllById(@NonNull Iterable<Long> ids) { throw new UnsupportedOperationException(); }

    @Override
    public @NonNull List<Merchant> findAll(@NonNull org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }

    @Override
    public @NonNull org.springframework.data.domain.Page<Merchant> findAll(@NonNull org.springframework.data.domain.Pageable pageable) { throw new UnsupportedOperationException(); }

    @Override
    public @NonNull <S extends Merchant> Optional<S> findOne(@NonNull org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }

    @Override
    public @NonNull <S extends Merchant> org.springframework.data.domain.Page<S> findAll(@NonNull org.springframework.data.domain.Example<S> example, @NonNull org.springframework.data.domain.Pageable pageable) { throw new UnsupportedOperationException(); }

    @Override
    public <S extends Merchant> long count(@NonNull org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }

    @Override
    public <S extends Merchant> boolean exists(@NonNull org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }

    @Override
    public @NonNull <S extends Merchant, R> R findBy(@NonNull org.springframework.data.domain.Example<S> example, @NonNull java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw new UnsupportedOperationException(); }

    @Override
    public void deleteAll(@NonNull Iterable<? extends Merchant> entities) { throw new UnsupportedOperationException(); }

    @Override
    public void deleteAllById(@NonNull Iterable<? extends Long> ids) { throw new UnsupportedOperationException(); }
}