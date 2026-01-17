package com.shopizer.springboot.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cart Item Response DTO
 * Data Transfer Object for returning individual cart item details to the client.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: The system shall allow customers to manage cart items (Add, View, Update, Remove)
 * - FR-007: The system shall calculate and display cart total automatically
 *
 * USE CASES:
 * - UC05: View Cart - This DTO represents each item displayed in the cart listing
 * - UC06: Update Cart - This DTO shows the updated item details after modification
 *
 * ROLE IN ARCHITECTURE:
 * - OUTPUT DTO at the Controller layer (CartController)
 * - Created by CartServiceImpl via mapping from CartItem entity
 * - Enriched with product details (name, SKU) from Catalog module via ProductRepository
 * - Contains calculated subtotal (quantity × unitPrice) for FR-007
 * - Part of CartResponse DTO (nested in items list)
 *
 * CROSS-MODULE INTEGRATION:
 * - Fetches productName and productSku from Catalog module's Product entity
 * - Demonstrates loose coupling through DTO pattern (no direct entity exposure)
 */
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private String productSku;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CartItemResponse() {}

    public CartItemResponse(Long id, Long productId, String productName, String productSku,
                           Integer quantity, BigDecimal unitPrice, BigDecimal subtotal,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.productSku = productSku;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
