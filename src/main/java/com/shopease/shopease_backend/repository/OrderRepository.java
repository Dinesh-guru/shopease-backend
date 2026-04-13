package com.shopease.shopease_backend.repository;

import com.shopease.shopease_backend.entity.Order;
import com.shopease.shopease_backend.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Get all orders for a user, newest first
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Get orders by status (for admin)
    List<Order> findByStatus(OrderStatus status);

    // Get orders for a user by status
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);
}