package com.shopizer.springboot.merchant.entity;

import com.shopizer.springboot.catalog.entity.Product;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks product view events for conversion reporting (FR-018)
 */
@Entity
@Table(name = "product_view_events")
public class ProductViewEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "store_id")
    private Long storeId;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public ProductViewEvent() {
        this.viewedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Long getStoreId() { return storeId; }
    public void setStoreId(Long storeId) { this.storeId = storeId; }

    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}
