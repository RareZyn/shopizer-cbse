package com.shopizer.springboot.order.service;

import com.shopizer.springboot.order.entity.Order;
import java.util.List;
import java.util.Optional;

/**
 * Order Service Interface
 * FR-010 to FR-014: Order management
 */
public interface OrderService {

    Order createOrder(Order order);
    Optional<Order> getOrderById(Long id);
    Optional<Order> getOrderByOrderNumber(String orderNumber);
    List<Order> getOrdersByCustomerId(Long customerId);
    List<Order> getAllOrders();
    Order updateOrderStatus(Long id, String status);
    void deleteOrder(Long id);
}
