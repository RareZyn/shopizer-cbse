package com.shopizer.springboot.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart Response DTO
 * Complete Data Transfer Object representing the entire shopping cart with all details.
 *
 * FUNCTIONAL REQUIREMENTS:
 * - FR-006: The system shall allow customers to manage cart items (Add, View, Update, Remove)
 * - FR-007: The system shall calculate and display cart total automatically
 * - FR-009: The system shall persist cart data for logged-in customers
 *
 * USE CASES:
 * - UC05: View Cart - This is the main DTO returned when customer views their cart
 *         Contains complete cart information: items, total, item count, timestamps
 * - UC04: Add to Cart - Returned after successfully adding item to show updated cart
 * - UC06: Update Cart - Returned after update/remove operations to show current cart state
 *
 * ROLE IN ARCHITECTURE:
 * - OUTPUT DTO at the Controller layer (CartController)
 * - Created by CartServiceImpl.getCartResponse()
 * - Aggregates multiple CartItemResponse DTOs in items list
 * - Auto-calculates total (sum of all item subtotals) - fulfills FR-007
 * - Auto-calculates itemCount (number of items in cart)
 *
 * AUTO-CALCULATED FIELDS (FR-007):
 * - total: Sum of all CartItemResponse.subtotal values (BigDecimal for precision)
 * - itemCount: Size of items list
 *
 * EMPTY CART HANDLING (UC05 Exception Flow):
 * - When cart has no items: items=[], total=0.00, itemCount=0
 * - Frontend can display "Please add your desired products inside your cart"
 */
public class CartResponse {

    private Long id;
    private Long customerId;
    private String sessionId;
    private String status;
    private List<CartItemResponse> items = new ArrayList<>();
    private BigDecimal total;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public CartResponse() {}

    public CartResponse(Long id, Long customerId, String sessionId, String status,
                       List<CartItemResponse> items, BigDecimal total, Integer itemCount,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.sessionId = sessionId;
        this.status = status;
        this.items = items;
        this.total = total;
        this.itemCount = itemCount;
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

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
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
