package com.shopizer.merchant.dto;

import java.math.BigDecimal;

/**
 * FR-018: Product sales report response
 * Contains per-product sales metrics
 */
public class ProductReportResponse {
    private Long productId;
    private String productName;
    private String sku;
    private Long unitsSold;
    private BigDecimal totalRevenue;
    private BigDecimal averagePrice;
    private Integer orderCount;

    // Constructors
    public ProductReportResponse() {}

    public ProductReportResponse(Long productId, String productName, String sku, 
                                 Long unitsSold, BigDecimal totalRevenue, 
                                 BigDecimal averagePrice, Integer orderCount) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.unitsSold = unitsSold;
        this.totalRevenue = totalRevenue;
        this.averagePrice = averagePrice;
        this.orderCount = orderCount;
    }

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public Long getUnitsSold() { return unitsSold; }
    public void setUnitsSold(Long unitsSold) { this.unitsSold = unitsSold; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getAveragePrice() { return averagePrice; }
    public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }

    public Integer getOrderCount() { return orderCount; }
    public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
}
