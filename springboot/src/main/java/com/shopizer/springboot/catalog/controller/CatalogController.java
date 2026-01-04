package com.shopizer.springboot.catalog.controller;

import com.shopizer.springboot.catalog.entity.Category;
import com.shopizer.springboot.catalog.entity.Product;
import com.shopizer.springboot.catalog.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Catalog REST Controller
 * FR-001: Product CRUD operations
 * FR-002: Category CRUD operations
 * FR-003: Search products by keyword
 * FR-005: Track product stock levels
 */
@RestController
@RequestMapping("/api/v1/catalog")
@Tag(name = "Catalog", description = "Product and Category management APIs (FR-001 to FR-005)")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    // ==================== PRODUCT ENDPOINTS (FR-001) ====================

    @PostMapping("/products")
    @Operation(summary = "Create a new product", description = "FR-001: Create product")
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        return new ResponseEntity<>(catalogService.createProduct(product), HttpStatus.CREATED);
    }

    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "FR-001: List all active products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(catalogService.getAllProducts());
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product by ID", description = "FR-001: Get product details")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return catalogService.getProductById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update product", description = "FR-001: Update product details")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        return ResponseEntity.ok(catalogService.updateProduct(id, product));
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete product", description = "FR-001: Delete product")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        catalogService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== CATEGORY ENDPOINTS (FR-002) ====================

    @PostMapping("/categories")
    @Operation(summary = "Create a new category", description = "FR-002: Create category")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        return new ResponseEntity<>(catalogService.createCategory(category), HttpStatus.CREATED);
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories", description = "FR-002: List all active categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(catalogService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    @Operation(summary = "Get category by ID", description = "FR-002: Get category details")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        return catalogService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/categories/{id}")
    @Operation(summary = "Update category", description = "FR-002: Update category details")
    public ResponseEntity<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return ResponseEntity.ok(catalogService.updateCategory(id, category));
    }

    @DeleteMapping("/categories/{id}")
    @Operation(summary = "Delete category", description = "FR-002: Delete category")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        catalogService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== SEARCH ENDPOINT (FR-003) ====================

    @GetMapping("/products/search")
    @Operation(summary = "Search products", description = "FR-003: Search products by keyword")
    public ResponseEntity<List<Product>> searchProducts(@RequestParam String keyword) {
        return ResponseEntity.ok(catalogService.searchProducts(keyword));
    }

    // ==================== STOCK ENDPOINT (FR-005) ====================

    @GetMapping("/products/low-stock")
    @Operation(summary = "Get low stock products", description = "FR-005: Get products with low stock")
    public ResponseEntity<List<Product>> getLowStockProducts() {
        return ResponseEntity.ok(catalogService.getLowStockProducts());
    }
}
