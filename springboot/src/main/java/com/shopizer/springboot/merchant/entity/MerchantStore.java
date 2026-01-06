package com.shopizer.springboot.merchant.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Merchant Store Entity
 * FR-015: The system shall allow merchants to manage their store profile
 * FR-016: The system shall allow merchants to manage their product inventory
 */
@Entity
@Table(
    name = "merchant_stores", uniqueConstraints = {
        @UniqueConstraint(name = "uk_store_code", columnNames = "store_code")
    }
) 
public class MerchantStore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK merchant_id -> merchants.id
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "store_name", nullable = false)
    private String storeName;

    @Column(columnDefinition = "TEXT")
    private String address;
    
    @Column(name = "store_code", nullable = false, length = 50, unique = true)
    private String storeCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(length = 3)
    private String currency;

    @Column(name = "default_language", length = 50)
    private String defaultLanguage;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public MerchantStore() {}

    // getters/setters
    public Long getId() { return id; }

    public Merchant getMerchant() { return merchant; }
    public void setMerchant(Merchant merchant) { this.merchant = merchant; }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    public String getAddress() {return address;}
    public void setAddress(String address) {this.address = address;
    }

    public String getStoreCode() { return storeCode; }
    public void setStoreCode(String storeCode) { this.storeCode = storeCode; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDefaultLanguage() { return defaultLanguage; }
    public void setDefaultLanguage(String defaultLanguage) { this.defaultLanguage = defaultLanguage; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean active) { isActive = active; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    // Entity fields will be implemented here
    // - id
    // - merchantId
    // - storeName
    // - storeCode
    // - description
    // - logo
    // - address
    // - currency
    // - defaultLanguage
    // - isActive
    // - createdAt
    // - updatedAt
}
