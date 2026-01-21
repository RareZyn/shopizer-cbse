package com.shopizer.merchant.repository;

import com.shopizer.common.entity.Order;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Manual JPA implementation for OSGi without Spring Data runtime.
 * Supports only the methods needed by merchant module usage.
 */
public class OrderRepositoryImpl implements OrderRepository {

    private final EntityManagerFactory entityManagerFactory;

    public OrderRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    private EntityManager em() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public List<Order> findByStoreId(Long storeId) {
        EntityManager em = em();
        try {
            // Fetch join items and products so they remain available after the EntityManager closes
            TypedQuery<Order> q = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.items i " +
                "LEFT JOIN FETCH i.product p " +
                "LEFT JOIN FETCH p.category " +
                "WHERE o.store.id = :storeId",
                Order.class);
            q.setParameter("storeId", storeId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(Order.class, id));
        } finally {
            em.close();
        }
    }

    private Order doSave(Order order) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            if (order.getId() == null) {
                em.persist(order);
            } else {
                order = em.merge(order);
            }
            em.getTransaction().commit();
            return order;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error saving order", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Order> findAll() {
        EntityManager em = em();
        try {
            return em.createQuery("SELECT o FROM Order o", Order.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            Order o = em.find(Order.class, id);
            if (o != null) {
                em.remove(o);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting order", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Order entity) {
        deleteById(entity.getId());
    }

    @Override
    public void deleteAll() {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Order").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting orders", e);
        } finally {
            em.close();
        }
    }

    // Unsupported operations required by JpaRepository
    @Override public void flush() { /* no-op */ }
    @Override public <S extends Order> S saveAndFlush(S entity) { return (S) save(entity); }
    @Override public <S extends Order> List<S> saveAllAndFlush(Iterable<S> entities) { throw new UnsupportedOperationException(); }
    @Override public void deleteAllInBatch(Iterable<Order> entities) { throw new UnsupportedOperationException(); }
    @Override public void deleteAllByIdInBatch(Iterable<Long> longs) { throw new UnsupportedOperationException(); }
    @Override public void deleteAllInBatch() { throw new UnsupportedOperationException(); }
    @Override public Order getOne(Long aLong) { return findById(aLong).orElse(null); }
    @Override public Order getById(Long aLong) { return findById(aLong).orElse(null); }
    @Override public Order getReferenceById(Long aLong) { return getById(aLong); }
    @Override public <S extends Order> List<S> findAll(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends Order> List<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }
    @Override public <S extends Order> S save(S entity) { return (S) doSave((Order) entity); }
    @Override public <S extends Order> List<S> saveAll(Iterable<S> entities) { throw new UnsupportedOperationException(); }
    @Override public List<Order> findAllById(Iterable<Long> ids) { throw new UnsupportedOperationException(); }
    @Override public List<Order> findAll(org.springframework.data.domain.Sort sort) { throw new UnsupportedOperationException(); }
    @Override public org.springframework.data.domain.Page<Order> findAll(org.springframework.data.domain.Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends Order> Optional<S> findOne(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends Order> org.springframework.data.domain.Page<S> findAll(org.springframework.data.domain.Example<S> example, org.springframework.data.domain.Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public <S extends Order> long count(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends Order> boolean exists(org.springframework.data.domain.Example<S> example) { throw new UnsupportedOperationException(); }
    @Override public <S extends Order, R> R findBy(org.springframework.data.domain.Example<S> example, java.util.function.Function<org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery<S>, R> queryFunction) { throw new UnsupportedOperationException(); }
    @Override public long count() { return findAll().size(); }
    @Override public boolean existsById(Long aLong) { return findById(aLong).isPresent(); }
    @Override public List<Order> findByCustomerId(Long customerId) { return queryList("SELECT o FROM Order o WHERE o.customer.id = :customerId", "customerId", customerId); }
    
    @Override
    public java.util.Optional<Order> findByOrderNumber(String orderNumber) {
        EntityManager em = em();
        try {
            TypedQuery<Order> q = em.createQuery(
                "SELECT o FROM Order o WHERE o.orderNumber = :orderNumber", Order.class);
            q.setParameter("orderNumber", orderNumber);
            return q.getResultStream().findFirst();
        } finally {
            em.close();
        }
    }
    
    @Override public List<Order> findByStatus(String status) { return queryList("SELECT o FROM Order o WHERE o.status = :status", "status", status); }

    @Override
    public List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return queryList("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC",
            Map.of("startDate", startDate, "endDate", endDate));
    }

    @Override
    public List<Order> findByCustomerIdAndDateRange(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        return queryList("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC",
            Map.of("customerId", customerId, "startDate", startDate, "endDate", endDate));
    }

    @Override
    public List<Order> findCompletedOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return queryList("SELECT o FROM Order o WHERE (o.status = 'PAID' OR o.status = 'PROCESSING' OR o.status = 'SHIPPED' OR o.status = 'DELIVERED') AND o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC",
            Map.of("startDate", startDate, "endDate", endDate));
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> ids) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            for (Long id : ids) {
                Order o = em.find(Order.class, id);
                if (o != null) {
                    em.remove(o);
                }
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting orders", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteAll(Iterable<? extends Order> entities) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            for (Order o : entities) {
                Order managed = em.find(Order.class, o.getId());
                if (managed != null) {
                    em.remove(managed);
                }
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting orders", e);
        } finally {
            em.close();
        }
    }

    private List<Order> queryList(String jpql, String paramName, Object value) {
        EntityManager em = em();
        try {
            TypedQuery<Order> q = em.createQuery(jpql, Order.class);
            q.setParameter(paramName, value);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    private List<Order> queryList(String jpql, Map<String, Object> params) {
        EntityManager em = em();
        try {
            TypedQuery<Order> q = em.createQuery(jpql, Order.class);
            params.forEach(q::setParameter);
            return q.getResultList();
        } finally {
            em.close();
        }
    }
}
