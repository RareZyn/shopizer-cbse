package com.shopizer.catalog.api;

import com.shopizer.catalog.dto.*;
import java.util.List;

/**
 * OSGI Service Interface for Catalog Module
 * Provides product and category management operations (FR-001 to FR-005)
 */
public interface CatalogService {

    // Product Management
    ProductResponse createProduct(ProductRequest request);
    ProductResponse getProductById(Long id);
    ProductResponse updateProduct(Long id, ProductRequest request);
    void deleteProduct(Long id);
    List<ProductResponse> searchProducts(String keyword);
    List<ProductResponse> getProductsByCategory(Long categoryId);
    List<ProductResponse> getAllProducts();

    // Category Management
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse getCategoryById(Long id);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);
    List<CategoryResponse> getAllCategories();
    List<CategoryResponse> getSubCategories(Long parentId);

    // Inventory Management
    void updateStock(Long productId, int quantity);
    boolean checkStock(Long productId, int quantity);
}
