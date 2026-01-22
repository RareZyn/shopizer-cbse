package com.shopizer.merchant.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * FR-018: Detailed product analytics
 * Includes daily sales breakdown, conversion metrics, and trends
 */
public class ProductAnalyticsResponse {
    private Long productId;
    private String productName;
    private String sku;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Summary metrics
    private Long totalUnitsSold;
    private BigDecimal totalRevenue;
    private BigDecimal averageUnitPrice;
    private Integer totalOrders;
    private Integer pageViews;
    private BigDecimal conversionRate; // views to orders ratio
    
    // Daily breakdown
    private List<DailySalesMetric> dailyMetrics = new ArrayList<>();

    public ProductAnalyticsResponse() {}

    public ProductAnalyticsResponse(Long productId, String productName, String sku) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
    }

    // Getters and Setters
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Long getTotalUnitsSold() { return totalUnitsSold; }
    public void setTotalUnitsSold(Long totalUnitsSold) { this.totalUnitsSold = totalUnitsSold; }

    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

    public BigDecimal getAverageUnitPrice() { return averageUnitPrice; }
    public void setAverageUnitPrice(BigDecimal averageUnitPrice) { this.averageUnitPrice = averageUnitPrice; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

    public Integer getPageViews() { return pageViews; }
    public void setPageViews(Integer pageViews) { this.pageViews = pageViews; }

    public BigDecimal getConversionRate() { return conversionRate; }
    public void setConversionRate(BigDecimal conversionRate) { this.conversionRate = conversionRate; }

    public List<DailySalesMetric> getDailyMetrics() { return dailyMetrics; }
    public void setDailyMetrics(List<DailySalesMetric> dailyMetrics) { this.dailyMetrics = dailyMetrics; }

    /**
     * Inner class for daily sales breakdown
     */
    public static class DailySalesMetric {
        private LocalDate date;
        private Long unitsSold;
        private BigDecimal revenue;
        private Integer orders;
        private Integer views;

        public DailySalesMetric(LocalDate date, Long unitsSold, BigDecimal revenue, Integer orders, Integer views) {
            this.date = date;
            this.unitsSold = unitsSold;
            this.revenue = revenue;
            this.orders = orders;
            this.views = views;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }

        public Long getUnitsSold() { return unitsSold; }
        public void setUnitsSold(Long unitsSold) { this.unitsSold = unitsSold; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

        public Integer getOrders() { return orders; }
        public void setOrders(Integer orders) { this.orders = orders; }

        public Integer getViews() { return views; }
        public void setViews(Integer views) { this.views = views; }
    }
}
