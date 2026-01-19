package com.shopizer.merchant.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class SalesReportResponse {
    private Long storeId;
    private String storeName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalOrders;
    private Integer completedOrders;
    private Integer cancelledOrders;
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;
    private List<ProductSalesResponse> topProducts;
}
