package com.shopizer.rest.servlet;

import com.shopizer.catalog.api.CatalogService;
import com.shopizer.catalog.dto.CategoryRequest;
import com.shopizer.catalog.dto.CategoryResponse;
import com.shopizer.catalog.dto.ProductRequest;
import com.shopizer.catalog.dto.ProductResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * REST Controller for Catalog Service
 *
 * Endpoints:
 * GET    /api/v1/products           - Get all products
 * GET    /api/v1/products/{id}      - Get product by ID
 * POST   /api/v1/products           - Create product
 * PUT    /api/v1/products/{id}      - Update product
 * DELETE /api/v1/products/{id}      - Delete product
 * GET    /api/v1/products/search?q={keyword} - Search products
 * GET    /api/v1/products/category/{id} - Get products by category
 * GET    /api/v1/products/low-stock     - Get low stock products (FR-005)
 *
 * GET    /api/v1/categories         - Get all categories
 * GET    /api/v1/categories/{id}    - Get category by ID
 * POST   /api/v1/categories         - Create category
 * PUT    /api/v1/categories/{id}    - Update category
 * DELETE /api/v1/categories/{id}    - Delete category
 */
public class CatalogServlet extends BaseServlet {

    private CatalogService catalogService;

    public CatalogServlet(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String pathInfo = request.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // GET /api/v1/products or /api/v1/categories
                handleGetAll(request, response);
            } else if (pathInfo.contains("/search")) {
                // GET /api/v1/products/search?q=keyword
                handleSearch(request, response);
            } else if (pathInfo.contains("/low-stock")) {
                // GET /api/v1/products/low-stock
                handleGetLowStock(request, response);
            } else if (pathInfo.contains("/category/")) {
                // GET /api/v1/products/category/{id}
                handleGetByCategory(request, response);
            } else {
                // GET /api/v1/products/{id} or /api/v1/categories/{id}
                handleGetById(request, response);
            }
        } catch (Exception e) {
            logger.error("Error in GET request", e);
            sendInternalError(response, "Error processing request: " + e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String servletPath = request.getServletPath();

            if (servletPath.contains("/products")) {
                // POST /api/v1/products
                ProductRequest productRequest = readJsonBody(request, ProductRequest.class);
                ProductResponse created = catalogService.createProduct(productRequest);
                sendCreated(response, created);
            } else if (servletPath.contains("/categories")) {
                // POST /api/v1/categories
                CategoryRequest categoryRequest = readJsonBody(request, CategoryRequest.class);
                CategoryResponse created = catalogService.createCategory(categoryRequest);
                sendCreated(response, created);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in POST request", e);
            sendInternalError(response, "Error creating resource: " + e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String servletPath = request.getServletPath();
            Long id = extractIdFromPath(request, servletPath);

            if (id == null) {
                sendBadRequest(response, "ID is required for update");
                return;
            }

            if (servletPath.contains("/products")) {
                // PUT /api/v1/products/{id}
                ProductRequest productRequest = readJsonBody(request, ProductRequest.class);
                ProductResponse updated = catalogService.updateProduct(id, productRequest);
                sendSuccess(response, updated);
            } else if (servletPath.contains("/categories")) {
                // PUT /api/v1/categories/{id}
                CategoryRequest categoryRequest = readJsonBody(request, CategoryRequest.class);
                CategoryResponse updated = catalogService.updateCategory(id, categoryRequest);
                sendSuccess(response, updated);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in PUT request", e);
            sendInternalError(response, "Error updating resource: " + e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        enableCors(response);

        try {
            String servletPath = request.getServletPath();
            Long id = extractIdFromPath(request, servletPath);

            if (id == null) {
                sendBadRequest(response, "ID is required for delete");
                return;
            }

            if (servletPath.contains("/products")) {
                // DELETE /api/v1/products/{id}
                catalogService.deleteProduct(id);
                sendNoContent(response);
            } else if (servletPath.contains("/categories")) {
                // DELETE /api/v1/categories/{id}
                catalogService.deleteCategory(id);
                sendNoContent(response);
            } else {
                sendBadRequest(response, "Invalid endpoint");
            }
        } catch (Exception e) {
            logger.error("Error in DELETE request", e);
            sendInternalError(response, "Error deleting resource: " + e.getMessage());
        }
    }

    // ========== GET Helper Methods ==========

    private void handleGetAll(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String servletPath = request.getServletPath();

        if (servletPath.contains("/products")) {
            List<ProductResponse> products = catalogService.getAllProducts();
            sendSuccess(response, products);
        } else if (servletPath.contains("/categories")) {
            List<CategoryResponse> categories = catalogService.getAllCategories();
            sendSuccess(response, categories);
        } else {
            sendBadRequest(response, "Invalid endpoint");
        }
    }

    private void handleGetById(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String servletPath = request.getServletPath();
        Long id = extractIdFromPath(request, servletPath);

        if (id == null) {
            sendBadRequest(response, "Invalid ID");
            return;
        }

        if (servletPath.contains("/products")) {
            ProductResponse product = catalogService.getProductById(id);
            if (product != null) {
                sendSuccess(response, product);
            } else {
                sendNotFound(response, "Product not found with ID: " + id);
            }
        } else if (servletPath.contains("/categories")) {
            CategoryResponse category = catalogService.getCategoryById(id);
            if (category != null) {
                sendSuccess(response, category);
            } else {
                sendNotFound(response, "Category not found with ID: " + id);
            }
        } else {
            sendBadRequest(response, "Invalid endpoint");
        }
    }

    private void handleSearch(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String keyword = request.getParameter("q");
        if (keyword == null || keyword.isEmpty()) {
            sendBadRequest(response, "Search keyword is required");
            return;
        }

        List<ProductResponse> results = catalogService.searchProducts(keyword);
        sendSuccess(response, results);
    }

    private void handleGetByCategory(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String[] parts = pathInfo.split("/");

        if (parts.length < 3) {
            sendBadRequest(response, "Category ID is required");
            return;
        }

        try {
            Long categoryId = Long.parseLong(parts[2]);
            List<ProductResponse> products = catalogService.getProductsByCategory(categoryId);
            sendSuccess(response, products);
        } catch (NumberFormatException e) {
            sendBadRequest(response, "Invalid category ID");
        }
    }

    /**
     * FR-005: Get low stock products
     * Returns products where stockQuantity <= reorderLevel (low_stock_threshold)
     */
    private void handleGetLowStock(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<ProductResponse> products = catalogService.getLowStockProducts();
        sendSuccess(response, products);
    }
}
