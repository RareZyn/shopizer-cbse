package com.shopizer.customer.repository;

import com.shopizer.common.entity.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * JPA Implementation of Payment Repository
 * FR-023: The system shall allow customers to view payment history
 */
public class PaymentRepositoryImpl implements PaymentRepository {

    private static final Logger logger = LoggerFactory.getLogger(PaymentRepositoryImpl.class);
    private final EntityManagerFactory entityManagerFactory;

    public PaymentRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    private EntityManager getEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public Optional<Payment> findById(Long id) {
        EntityManager em = getEntityManager();
        try {
            Payment payment = em.find(Payment.class, id);
            return Optional.ofNullable(payment);
        } catch (Exception e) {
            logger.error("Error finding payment by ID: {}", id, e);
            return Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Payment> findAll() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p ORDER BY p.createdAt DESC", Payment.class);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding all payments", e);
            return List.of();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Payment> findByCustomerId(Long customerId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Payment> query = em.createQuery(
                "SELECT p FROM Payment p WHERE p.order.customer.id = :customerId ORDER BY p.createdAt DESC",
                Payment.class);
            query.setParameter("customerId", customerId);
            return query.getResultList();
        } catch (Exception e) {
            logger.error("Error finding payments for customer: {}", customerId, e);
            return List.of();
        } finally {
            em.close();
        }
    }

    @Override
    public Payment save(Payment payment) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            if (payment.getId() == null) {
                em.persist(payment);
            } else {
                payment = em.merge(payment);
            }
            em.getTransaction().commit();
            return payment;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error saving payment", e);
            throw new RuntimeException("Failed to save payment", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(Payment payment) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Payment managedPayment = em.contains(payment) ? payment : em.merge(payment);
            em.remove(managedPayment);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error deleting payment", e);
            throw new RuntimeException("Failed to delete payment", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Payment payment = em.find(Payment.class, id);
            if (payment != null) {
                em.remove(payment);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            logger.error("Error deleting payment by ID: {}", id, e);
            throw new RuntimeException("Failed to delete payment", e);
        } finally {
            em.close();
        }
    }
}
