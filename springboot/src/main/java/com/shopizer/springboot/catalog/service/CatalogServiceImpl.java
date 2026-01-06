package com.shopizer.springboot.catalog.service;

import com.shopizer.springboot.catalog.entity.Category;
import com.shopizer.springboot.catalog.entity.Product;
import com.shopizer.springboot.catalog.repository.CategoryRepository;
import com.shopizer.springboot.catalog.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Catalog Service Implementation
 * FR-001 to FR-005: Catalog management functionality
 */
@Service
public class CatalogServiceImpl implements CatalogService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public CatalogServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Product operations (FR-001)
    @Override
    @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public Product createProduct(Product product) {
        // Validate required fields
        if (product.getSku() == null || product.getSku().trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required and cannot be empty");
        }
        
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name is required and cannot be empty");
        }
        
        if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Product price is required and must be greater than or equal to 0");
        }
        
        // Trim SKU for consistency
        String trimmedSku = product.getSku().trim();
        
        // Check for duplicate SKU before creating the entity
        // This prevents duplicate creation at the application level
        Optional<Product> existingProduct = productRepository.findBySku(trimmedSku);
        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("Product with SKU '" + trimmedSku + "' already exists");
        }
        
        // Create a fresh product entity to avoid any entity state issues
        // This ensures we're always creating a truly new entity and prevents duplicates
        // Using a new entity prevents any potential issues with detached/managed entity state
        Product newProduct = new Product();
        newProduct.setSku(trimmedSku);
        newProduct.setName(product.getName() != null ? product.getName().trim() : null);
        newProduct.setDescription(product.getDescription() != null ? product.getDescription().trim() : null);
        newProduct.setPrice(product.getPrice());
        newProduct.setStockQuantity(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
        newProduct.setLowStockThreshold(product.getLowStockThreshold() != null ? product.getLowStockThreshold() : 10);
        newProduct.setIsActive(product.getIsActive() != null ? product.getIsActive() : true);
        
        // Handle category reference properly
        // If category is provided with an ID > 0, fetch the managed entity from database
        if (product.getCategory() != null) {
            Long categoryId = product.getCategory().getId();
            
            if (categoryId != null && categoryId > 0) {
                // Category ID provided - fetch from database
                Optional<Category> categoryOpt = categoryRepository.findById(categoryId);
                if (categoryOpt.isPresent()) {
                    // Use managed entity from database to avoid detached entity issues
                    newProduct.setCategory(categoryOpt.get());
                } else {
                    throw new IllegalArgumentException("Category with id " + categoryId + " not found");
                }
            }
            // If category ID is 0 or null, newProduct.category will remain null (default)
        }
        
        // Save the product - use saveAndFlush to ensure immediate persistence
        // This helps prevent duplicate creation in concurrent scenarios
        // The database unique constraint on SKU will also prevent duplicates at the DB level
        Product savedProduct = productRepository.saveAndFlush(newProduct);
        
        return savedProduct;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAllActiveWithCategory();
    }

    @Override
    public List<Product> listAllProducts() {
        return productRepository.findAllWithCategory();
    }

    @Override
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    @Transactional
    public Product updateProduct(Long id, Product product) {
        product.setId(id);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    // Category operations (FR-002)
    @Override
    @Transactional
    public Category createCategory(Category category) {
        // Force new entity creation - ignore any ID sent in request (including id: 0)
        // This ensures Hibernate treats it as a new entity, not an update
        category.setId(null);
        
        // Timestamps will be automatically set by @PrePersist in Category entity
        // No need to reset them as Category doesn't expose setters for these fields
        
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    @Override
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, Category category) {
        category.setId(id);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    // Search (FR-003)
    @Override
    public List<Product> searchProducts(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseWithCategory(keyword);
    }

    @Override
    public List<Product> searchProduct(String keyword) {
        return productRepository.findByNameContainingIgnoreCaseWithCategory(keyword);
    }

    // Browse by category (FR-004)
    @Override
    public List<Product> browseProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryIdWithCategory(categoryId);
    }

    // Stock management (FR-005)
    @Override
    public List<Product> getLowStockProducts() {
        // Get products where stockQuantity <= lowStockThreshold
        // This compares each product's stock against its own threshold
        return productRepository.findLowStockProductsWithCategory();
    }
}
