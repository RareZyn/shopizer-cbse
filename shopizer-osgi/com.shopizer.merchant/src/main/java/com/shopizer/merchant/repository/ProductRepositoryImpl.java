package com.shopizer.merchant.repository;

import com.shopizer.common.entity.Product;
import com.shopizer.common.entity.Category;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.lang.NonNull;

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
    public @NonNull <S extends Product> S save(@NonNull S product) {
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
                product = (S) em.merge(product);
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
    public @NonNull <S extends Product> S saveAndFlush(@NonNull S entity) {
        return save(entity);
    }

    @Override
    public @NonNull <S extends Product> List<S> saveAllAndFlush(@NonNull Iterable<S> entities) {
        List<S> result = new java.util.ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public void deleteInBatch(@NonNull Iterable<Product> entities) {
        for (Product product : entities) {
            delete(product);
        }
    }

    @Override
    public void deleteAllInBatch(@NonNull Iterable<Product> entities) {
        deleteInBatch(entities);
    }

    @Override
    public void deleteAllByIdInBatch(@NonNull Iterable<Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void flush() {
        // No-op for manual JPA
    }

    @Override
    public @NonNull <S extends Product> List<S> saveAll(@NonNull Iterable<S> entities) {
        List<S> result = new java.util.ArrayList<>();
        for (S entity : entities) {
            result.add(save(entity));
        }
        return result;
    }

    @Override
    public @NonNull Optional<Product> findById(@NonNull Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(Product.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(@NonNull Long id) {
        return findById(id).isPresent();
    }

    @Override
    public @NonNull List<Product> findAll() {
        EntityManager em = em();
        try {
            TypedQuery<Product> q = em.createQuery("SELECT p FROM Product p", Product.class);
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public @NonNull List<Product> findAllById(@NonNull Iterable<Long> ids) {
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
    public void deleteById(@NonNull Long id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    public void delete(@NonNull Product product) {
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
    public void deleteAllById(@NonNull Iterable<? extends Long> ids) {
        for (Long id : ids) {
            deleteById(id);
        }
    }

    @Override
    public void deleteAll(@NonNull Iterable<? extends Product> entities) {
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
    public @NonNull List<Product> findAll(@NonNull Sort sort) {
        return findAll();
    }

    @Override
    public @NonNull Page<Product> findAll(@NonNull Pageable pageable) {
        List<Product> all = findAll();
        return new PageImpl<>(all, pageable, all.size());
    }

    @Override
    public @NonNull <S extends Product> Optional<S> findOne(@NonNull Example<S> example) {
        List<S> results = findAll(example);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public @NonNull <S extends Product> List<S> findAll(@NonNull Example<S> example) {
        return new java.util.ArrayList<>();
    }

    @Override
    public @NonNull <S extends Product> List<S> findAll(@NonNull Example<S> example, @NonNull Sort sort) {
        return new java.util.ArrayList<>();
    }

    @Override
    public @NonNull <S extends Product> Page<S> findAll(@NonNull Example<S> example, @NonNull Pageable pageable) {
        return new PageImpl<>(new java.util.ArrayList<>(), pageable, 0);
    }

    @Override
    public <S extends Product> long count(@NonNull Example<S> example) {
        return 0;
    }

    @Override
    public <S extends Product> boolean exists(@NonNull Example<S> example) {
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
        throw new UnsupportedOperationException("Unimplemented method 'deleteAllInBatch'");
    }

    @Override
    public @NonNull Product getById(@NonNull Long arg0) {
        return findById(arg0).orElseThrow(() ->
            new jakarta.persistence.EntityNotFoundException("Product not found with id: " + arg0));
    }

    @Override
    public @NonNull Product getOne(@NonNull Long arg0) {
        return findById(arg0).orElseThrow(() ->
            new jakarta.persistence.EntityNotFoundException("Product not found with id: " + arg0));
    }

    @Override
    public @NonNull Product getReferenceById(@NonNull Long id) {
        return getById(id);
    }

    @Override
    public @NonNull <S extends Product, R> R findBy(@NonNull Example<S> example, @NonNull Function<FetchableFluentQuery<S>, R> queryFunction) {
        throw new UnsupportedOperationException("Unimplemented method 'findBy'");
    }
}
