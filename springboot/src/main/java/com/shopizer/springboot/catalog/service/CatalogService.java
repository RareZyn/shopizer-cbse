package com.shopizer.springboot.catalog.service;

import com.shopizer.springboot.catalog.entity.Category;
import com.shopizer.springboot.catalog.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * Catalog Service Interface
 * FR-001: Product CRUD operations
 * FR-002: Category CRUD operations
 * FR-003: Search products by keyword
 * FR-005: Track product stock levels
 */
public interface CatalogService {

    // Product operations (FR-001)
    Product createProduct(Product product);
    List<Product> getAllProducts();
    Optional<Product> getProductById(Long id);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);

    // Category operations (FR-002)
    Category createCategory(Category category);
    List<Category> getAllCategories();
    Optional<Category> getCategoryById(Long id);
    Category updateCategory(Long id, Category category);
    void deleteCategory(Long id);

    // Search (FR-003)
    List<Product> searchProducts(String keyword);

    // Stock management (FR-005)
    List<Product> getLowStockProducts();
}
