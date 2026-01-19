package com.shopizer.customer.repository;

import com.shopizer.common.entity.Address;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class AddressRepositoryImpl implements AddressRepository {

    private final EntityManagerFactory entityManagerFactory;

    public AddressRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    private EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public Address save(Address address) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            if (address.getId() == null) {
                em.persist(address);
            } else {
                address = em.merge(address);
            }
            em.getTransaction().commit();
            return address;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error saving address", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Address> findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            Address address = em.find(Address.class, id);
            return Optional.ofNullable(address);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Address> findByCustomerId(Long customerId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Address> query = em.createQuery(
                "SELECT a FROM Address a WHERE a.customer.id = :customerId", Address.class);
            query.setParameter("customerId", customerId);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Address> findAll() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Address> query = em.createQuery(
                "SELECT a FROM Address a", Address.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Address address = em.find(Address.class, id);
            if (address != null) {
                em.remove(address);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting address", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void delete(Address address) {
        deleteById(address.getId());
    }

    @Override
    public long count() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(a) FROM Address a", Long.class);
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public void flush() {
        // No-op for manual JPA
    }

    @Override
    public <S extends Address> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends Address> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException("saveAllAndFlush not implemented");
    }

    @Override
    public void deleteAllInBatch(Iterable<Address> entities) {
        throw new UnsupportedOperationException("deleteAllInBatch not implemented");
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        throw new UnsupportedOperationException("deleteAllByIdInBatch not implemented");
    }

    @Override
    public void deleteAllInBatch() {
        throw new UnsupportedOperationException("deleteAllInBatch not implemented");
    }

    @Override
    public Address getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Address getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Address getReferenceById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.getReference(Address.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public <S extends Address> List<S> findAll(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("findAll with Example not implemented");
    }

    @Override
    public <S extends Address> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) {
        throw new UnsupportedOperationException("findAll with Example and Sort not implemented");
    }

    @Override
    public <S extends Address> List<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("saveAll not implemented");
    }

    @Override
    public List<Address> findAllById(Iterable<Long> ids) {
        throw new UnsupportedOperationException("findAllById not implemented");
    }

    @Override
    public List<Address> findAll(org.springframework.data.domain.Sort sort) {
        throw new UnsupportedOperationException("findAll with Sort not implemented");
    }

    @Override
    public org.springframework.data.domain.Page<Address> findAll(org.springframework.data.domain.Pageable pageable) {
        throw new UnsupportedOperationException("findAll with Pageable not implemented");
    }

    @Override
    public <S extends Address> Optional<S> findOne(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("findOne with Example not implemented");
    }

    @Override
    public <S extends Address> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) {
        throw new UnsupportedOperationException("findAll with Example and Pageable not implemented");
    }

    @Override
    public <S extends Address> long count(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("count with Example not implemented");
    }

    @Override
    public <S extends Address> boolean exists(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("exists with Example not implemented");
    }

    @Override
    public <S extends Address, R> R findBy(org.springframework.data.domain.Example<S> example,
                                           java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("findBy not implemented");
    }

    @Override
    public void deleteAll(Iterable<? extends Address> entities) {
        throw new UnsupportedOperationException("deleteAll with entities not implemented");
    }

    @Override
    public void deleteAll() {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Address").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting all addresses", e);
        } finally {
            em.close();
        }
    }
}
