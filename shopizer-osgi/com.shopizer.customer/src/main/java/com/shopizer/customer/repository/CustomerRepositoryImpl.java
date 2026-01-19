package com.shopizer.customer.repository;

import com.shopizer.common.entity.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

public class CustomerRepositoryImpl implements CustomerRepository {

    private final EntityManagerFactory entityManagerFactory;

    public CustomerRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    private EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public Customer save(Customer customer) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            if (customer.getId() == null) {
                em.persist(customer);
            } else {
                customer = em.merge(customer);
            }
            em.getTransaction().commit();
            return customer;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error saving customer", e);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Customer> findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            Customer customer = em.find(Customer.class, id);
            return Optional.ofNullable(customer);
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c WHERE c.email = :email", Customer.class);
            query.setParameter("email", email);
            List<Customer> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Customer c WHERE c.email = :email", Long.class);
            query.setParameter("email", email);
            return query.getSingleResult() > 0;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Customer> findAll() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Customer> query = em.createQuery(
                "SELECT c FROM Customer c", Customer.class);
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
            Customer customer = em.find(Customer.class, id);
            if (customer != null) {
                em.remove(customer);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting customer", e);
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
    public void delete(Customer customer) {
        deleteById(customer.getId());
    }

    @Override
    public long count() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Customer c", Long.class);
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
    public <S extends Customer> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends Customer> List<S> saveAllAndFlush(Iterable<S> entities) {
        throw new UnsupportedOperationException("saveAllAndFlush not implemented");
    }

    @Override
    public void deleteAllInBatch(Iterable<Customer> entities) {
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
    public Customer getOne(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Customer getById(Long id) {
        return findById(id).orElse(null);
    }

    @Override
    public Customer getReferenceById(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.getReference(Customer.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public <S extends Customer> List<S> findAll(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("findAll with Example not implemented");
    }

    @Override
    public <S extends Customer> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) {
        throw new UnsupportedOperationException("findAll with Example and Sort not implemented");
    }

    @Override
    public <S extends Customer> List<S> saveAll(Iterable<S> entities) {
        throw new UnsupportedOperationException("saveAll not implemented");
    }

    @Override
    public List<Customer> findAllById(Iterable<Long> ids) {
        throw new UnsupportedOperationException("findAllById not implemented");
    }

    @Override
    public List<Customer> findAll(org.springframework.data.domain.Sort sort) {
        throw new UnsupportedOperationException("findAll with Sort not implemented");
    }

    @Override
    public org.springframework.data.domain.Page<Customer> findAll(org.springframework.data.domain.Pageable pageable) {
        throw new UnsupportedOperationException("findAll with Pageable not implemented");
    }

    @Override
    public <S extends Customer> Optional<S> findOne(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("findOne with Example not implemented");
    }

    @Override
    public <S extends Customer> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) {
        throw new UnsupportedOperationException("findAll with Example and Pageable not implemented");
    }

    @Override
    public <S extends Customer> long count(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("count with Example not implemented");
    }

    @Override
    public <S extends Customer> boolean exists(org.springframework.data.domain.Example<S> example) {
        throw new UnsupportedOperationException("exists with Example not implemented");
    }

    @Override
    public <S extends Customer, R> R findBy(org.springframework.data.domain.Example<S> example, 
                                            java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("findBy not implemented");
    }

    @Override
    public void deleteAll(Iterable<? extends Customer> entities) {
        throw new UnsupportedOperationException("deleteAll with entities not implemented");
    }

    @Override
    public void deleteAll() {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Customer").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting all customers", e);
        } finally {
            em.close();
        }
    }
}
