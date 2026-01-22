package com.shopizer.catalog.impl;

import com.shopizer.catalog.api.CatalogService;
import com.shopizer.catalog.dto.*;
import com.shopizer.catalog.repository.CategoryRepository;
import com.shopizer.catalog.repository.MerchantStoreRepository;
import com.shopizer.catalog.repository.ProductRepository;
import com.shopizer.common.entity.Category;
import com.shopizer.common.entity.MerchantStore;
import com.shopizer.common.entity.Product;
import com.shopizer.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CatalogServiceImpl implements CatalogService {

    private static final Logger logger = LoggerFactory.getLogger(CatalogServiceImpl.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MerchantStoreRepository merchantStoreRepository;

    public CatalogServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, MerchantStoreRepository merchantStoreRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.merchantStoreRepository = merchantStoreRepository;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        logger.info("Creating product: {}", request.getName());

        // Validate required fields (matching Spring Boot implementation)
        if (request.getSku() == null || request.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required and cannot be empty");
        }
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required and cannot be empty");
        }
        
        if (request.getPrice() == null || request.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price is required and must be greater than or equal to 0");
        }

        // Trim SKU for consistency
        String trimmedSku = request.getSku().trim();
        
        // Check for duplicate SKU before creating the entity
        Optional<Product> existingProduct = productRepository.findBySku(trimmedSku);
        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("Product with SKU '" + trimmedSku + "' already exists");
        }

        // Create a fresh product entity to avoid any entity state issues
        Product product = new Product();
        product.setSku(trimmedSku);
        product.setName(request.getName() != null ? request.getName().trim() : null);
        product.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0);
        product.setReorderLevel(request.getLowStockThreshold() != null ? request.getLowStockThreshold() : 10);
        product.setImageUrl(request.getImageUrl());
        product.setActive(request.getActive() != null ? request.getActive() : true);

        // Handle category reference properly
        // If category is provided with an ID > 0, fetch the managed entity from database
        if (request.getCategoryId() != null && request.getCategoryId() > 0) {
            Optional<Category> categoryOpt = categoryRepository.findById(request.getCategoryId());
            if (categoryOpt.isPresent()) {
                // Use managed entity from database to avoid detached entity issues
                product.setCategory(categoryOpt.get());
            } else {
                throw new IllegalArgumentException("Category with id " + request.getCategoryId() + " not found");
            }
        }
        // If category ID is 0 or null, product.category will remain null (default)

        // Handle store reference - REQUIRED field (matching Spring Boot implementation)
        // Store must be provided with an ID > 0
        if (request.getStoreId() != null && request.getStoreId() > 0) {
            Optional<MerchantStore> storeOpt = merchantStoreRepository.findById(request.getStoreId());
            if (storeOpt.isPresent()) {
                // Use managed entity from database to avoid detached entity issues
                product.setStore(storeOpt.get());
            } else {
                throw new IllegalArgumentException("Store with id " + request.getStoreId() + " not found");
            }
        } else {
            throw new IllegalArgumentException("Store ID is required and must be greater than 0");
        }

        // Save the product - use saveAndFlush to ensure immediate persistence
        // This helps prevent duplicate creation in concurrent scenarios
        // The database unique constraint on SKU will also prevent duplicates at the DB level
        Product savedProduct = productRepository.saveAndFlush(product);
        return mapToProductResponse(savedProduct);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        logger.info("Fetching product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToProductResponse(product);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        logger.info("Updating product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        // Validate SKU if provided
        if (request.getSku() != null) {
            String trimmedSku = request.getSku().trim();
            if (trimmedSku.isEmpty()) {
                throw new IllegalArgumentException("SKU cannot be empty");
            }
            // Check for duplicate SKU (excluding current product)
            Optional<Product> duplicateProduct = productRepository.findBySku(trimmedSku);
            if (duplicateProduct.isPresent() && !duplicateProduct.get().getId().equals(id)) {
                throw new IllegalArgumentException("Product with SKU '" + trimmedSku + "' already exists");
            }
            product.setSku(trimmedSku);
        }

        // Validate name if provided
        if (request.getName() != null) {
            String trimmedName = request.getName().trim();
            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException("Product name cannot be empty");
            }
            product.setName(trimmedName);
        }

        // Validate price if provided
        if (request.getPrice() != null) {
            if (request.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Product price cannot be negative");
            }
            product.setPrice(request.getPrice());
        }

        // Update other fields if provided
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription().trim());
        }

        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }

        if (request.getLowStockThreshold() != null) {
            product.setReorderLevel(request.getLowStockThreshold());
        }

        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        // Handle category reference properly
        if (request.getCategoryId() != null) {
            if (request.getCategoryId() > 0) {
                Optional<Category> categoryOpt = categoryRepository.findById(request.getCategoryId());
                if (categoryOpt.isPresent()) {
                    product.setCategory(categoryOpt.get());
                } else {
                    throw new IllegalArgumentException("Category with id " + request.getCategoryId() + " not found");
                }
            } else if (request.getCategoryId() == 0) {
                // Explicitly set to null if category ID is 0
                product.setCategory(null);
            }
        }

        // Handle store reference if provided
        if (request.getStoreId() != null) {
            if (request.getStoreId() > 0) {
                Optional<MerchantStore> storeOpt = merchantStoreRepository.findById(request.getStoreId());
                if (storeOpt.isPresent()) {
                    product.setStore(storeOpt.get());
                } else {
                    throw new IllegalArgumentException("Store with id " + request.getStoreId() + " not found");
                }
            } else {
                throw new IllegalArgumentException("Store ID must be greater than 0");
            }
        }

        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    @Override
    public void deleteProduct(Long id) {
        logger.info("Deleting product with id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        productRepository.delete(product);
    }

    @Override
    public List<ProductResponse> searchProducts(String keyword) {
        logger.info("Searching products with keyword: {}", keyword);
        List<Product> products = productRepository.findByNameContainingIgnoreCase(keyword);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        logger.info("Fetching products for category: {}", categoryId);
        List<Product> products = productRepository.findByCategoryId(categoryId);
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        logger.info("Fetching all products");
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        logger.info("Creating category: {}", request.getName());

        // Validate required fields
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required and cannot be empty");
        }

        Category category = new Category();
        category.setName(request.getName() != null ? request.getName().trim() : null);
        
        // Trim description if provided
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription().trim());
        }

        // Handle parent reference - OPTIONAL field
        // If parentId is provided with an ID > 0, fetch the managed entity from database
        if (request.getParentId() != null && request.getParentId() > 0) {
            Optional<Category> parentOpt = categoryRepository.findById(request.getParentId());
            if (parentOpt.isPresent()) {
                // Use managed entity from database to avoid detached entity issues
                category.setParent(parentOpt.get());
            } else {
                throw new IllegalArgumentException("Parent category with id " + request.getParentId() + " not found");
            }
        }
        // If parentId is null or 0, category.parent will remain null (root category)

        Category savedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        logger.info("Fetching category with id: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        return mapToCategoryResponse(category);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        logger.info("Updating category with id: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

        // Validate name if provided
        if (request.getName() != null) {
            String trimmedName = request.getName().trim();
            if (trimmedName.isEmpty()) {
                throw new IllegalArgumentException("Category name cannot be empty");
            }
            category.setName(trimmedName);
        }

        // Update description if provided
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription().trim());
        }

        // Handle parent reference - OPTIONAL field
        // If parentId is provided, update the parent relationship
        if (request.getParentId() != null) {
            if (request.getParentId() > 0) {
                // Parent ID provided - fetch from database
                Optional<Category> parentOpt = categoryRepository.findById(request.getParentId());
                if (parentOpt.isPresent()) {
                    // Use managed entity from database to avoid detached entity issues
                    category.setParent(parentOpt.get());
                } else {
                    throw new IllegalArgumentException("Parent category with id " + request.getParentId() + " not found");
                }
            } else if (request.getParentId() == 0) {
                // Explicitly set to null if category ID is 0 (make it a root category)
                category.setParent(null);
            }
        }
        // If parentId is null, don't change the existing parent relationship

        Category updatedCategory = categoryRepository.save(category);
        return mapToCategoryResponse(updatedCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        logger.info("Deleting category with id: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
        categoryRepository.delete(category);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        logger.info("Fetching all categories");
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStock(Long productId, int quantity) {
        logger.info("Updating stock for product: {} by quantity: {}", productId, quantity);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }

        product.setStockQuantity(newStock);
        productRepository.save(product);
    }

    @Override
    public boolean checkStock(Long productId, int quantity) {
        logger.info("Checking stock for product: {}, required quantity: {}", productId, quantity);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
        return product.getStockQuantity() >= quantity;
    }

    @Override
    public List<ProductResponse> getLowStockProducts() {
        logger.info("Fetching low stock products");
        List<Product> products = productRepository.findLowStockProducts();
        return products.stream()
                .map(this::mapToProductResponse)
                .collect(Collectors.toList());
    }

    // Helper methods
    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStockQuantity(product.getStockQuantity());
        response.setSku(product.getSku());
        response.setImageUrl(product.getImageUrl());
        response.setActive(product.getActive());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            response.setCategoryId(product.getCategory().getId());
            response.setCategoryName(product.getCategory().getName());
        }

        if (product.getStore() != null) {
            response.setStoreId(product.getStore().getId());
            response.setStoreName(product.getStore().getStoreName());
        }

        return response;
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());

        if (category.getParent() != null) {
            response.setParentId(category.getParent().getId());
            response.setParentName(category.getParent().getName());
        }

        return response;
    }
}
