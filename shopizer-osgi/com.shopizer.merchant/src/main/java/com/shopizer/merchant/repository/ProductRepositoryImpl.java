package com.shopizer.merchant.repository;

import com.shopizer.common.entity.Product;
import com.shopizer.common.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Manual JPA implementation for ProductRepository in OSGi without Spring Data runtime.
 */
public class ProductRepositoryImpl implements ProductRepository {

    private final EntityManagerFactory entityManagerFactory;

    public ProductRepositoryImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    private EntityManager em() {
        return entityManagerFactory.createEntityManager();
    }

    @Override
    public Product save(Product product) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            // Ensure Category reference is managed if only id provided
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                Category managed = em.getReference(Category.class, product.getCategory().getId());
                product.setCategory(managed);
            }
            if (product.getId() == null) {
                em.persist(product);
            } else {
                product = em.merge(product);
            }
            em.getTransaction().commit();
            return product;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error saving product", e);
        } finally {
            em.close();
        }
    }

    @Override
    public <S extends Product> S saveAndFlush(S entity) {
        return (S) save(entity);
    }

    @Override
    public <S extends Product> List<S> saveAllAndFlush(Iterable<S> entities) {
        List<S> result = new java.util.ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }

    @Override
    public void deleteInBatch(Iterable<Product> entities) {
        for (Product product : entities) {
            delete(product);
        }
    }

    @Override
    public void deleteAllInBatch(Iterable<Product> entities) {
        deleteInBatch(entities);
    }

    @Override
    public void deleteAllByIdInBatch(Iterable<Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void flush() {
        // No-op for manual JPA
    }

    @Override
    public <S extends Product> List<S> saveAll(Iterable<S> entities) {
        List<S> result = new java.util.ArrayList<>();
        for (S entity : entities) {
            result.add((S) save(entity));
        }
        return result;
    }

    @Override
    public Optional<Product> findById(Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(Product.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(Long id) {
        return findById(id).isPresent();
    }

    @Override
    public List<Product> findAll() {
        EntityManager em = em();
        try {
            TypedQuery<Product> q = em.createQuery("SELECT p FROM Product p", Product.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findAllById(Iterable<Long> ids) {
        List<Long> idList = new java.util.ArrayList<>();
        ids.forEach(idList::add);
        if (idList.isEmpty()) return new java.util.ArrayList<>();
        
        EntityManager em = em();
        try {
            TypedQuery<Product> q = em.createQuery(
                "SELECT p FROM Product p WHERE p.id IN :ids", Product.class);
            q.setParameter("ids", idList);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public long count() {
        EntityManager em = em();
        try {
            Long count = em.createQuery("SELECT COUNT(p) FROM Product p", Long.class).getSingleResult();
            return count != null ? count : 0;
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public void delete(Product product) {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            product = em.merge(product);
            em.remove(product);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting product", e);
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
    public void deleteAll(Iterable<? extends Product> entities) {
        for (Product entity : entities) {
            delete(entity);
        }
    }

    @Override
    public void deleteAll() {
        EntityManager em = em();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Product p").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error deleting all products", e);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findAll(Sort sort) {
        return findAll();
    }

    @Override
    public Page<Product> findAll(Pageable pageable) {
        List<Product> all = findAll();
        return new PageImpl<>(all, pageable, all.size());
    }

    @Override
    public <S extends Product> Optional<S> findOne(Example<S> example) {
        List<S> results = findAll(example);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public <S extends Product> List<S> findAll(Example<S> example) {
        return new java.util.ArrayList<>();
    }

    @Override
    public <S extends Product> List<S> findAll(Example<S> example, Sort sort) {
        return new java.util.ArrayList<>();
    }

    @Override
    public <S extends Product> Page<S> findAll(Example<S> example, Pageable pageable) {
        return new PageImpl<>(new java.util.ArrayList<>(), pageable, 0);
    }

    @Override
    public <S extends Product> long count(Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Product> boolean exists(Example<S> example) {
        return false;
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        EntityManager em = em();
        try {
            TypedQuery<Product> q = em.createQuery(
                "SELECT p FROM Product p WHERE p.sku = :sku", Product.class);
            q.setParameter("sku", sku);
            List<Product> results = q.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findByStoreId(Long storeId) {
        EntityManager em = em();
        try {
            TypedQuery<Product> q = em.createQuery(
                "SELECT p FROM Product p WHERE p.store.id = :storeId", Product.class);
            q.setParameter("storeId", storeId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findByStoreIdAndActive(Long storeId, Boolean active) {
        EntityManager em = em();
        try {
            TypedQuery<Product> q = em.createQuery(
                "SELECT p FROM Product p WHERE p.store.id = :storeId AND p.active = :active", Product.class);
            q.setParameter("storeId", storeId);
            q.setParameter("active", active);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findLowStockProducts(Long storeId) {
        EntityManager em = em();
        try {
            TypedQuery<Product> q = em.createQuery(
                "SELECT p FROM Product p WHERE p.store.id = :storeId AND p.stockQuantity <= p.reorderLevel AND p.active = true", 
                Product.class);
            q.setParameter("storeId", storeId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> findLowStockProductsByMerchant(Long merchantId) {
        EntityManager em = em();
        try {
            TypedQuery<Product> q = em.createQuery(
                "SELECT p FROM Product p WHERE p.store.merchant.id = :merchantId AND p.stockQuantity <= p.reorderLevel AND p.active = true", 
                Product.class);
            q.setParameter("merchantId", merchantId);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteAllInBatch() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllInBatch'");
    }

    @Override
    public Product getById(Long arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getById'");
    }

    @Override
    public Product getOne(Long arg0) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOne'");
    }

    @Override
    public Product getReferenceById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getReferenceById'");
    }

    @Override
    public <S extends Product, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findBy'");
    }
}
