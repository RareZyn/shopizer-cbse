package com.shopizer.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTrackingResponse {
    private String orderNumber;
    private String currentStatus;
    private String estimatedDeliveryDate;
    private List<StatusHistory> statusHistory = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatusHistory {
        private String status;
        private String description;
        private LocalDateTime timestamp;
    }
}
