package com.shopease.shopease_backend.service;

import com.shopease.shopease_backend.dto.*;
import com.shopease.shopease_backend.entity.*;
import com.shopease.shopease_backend.exception.*;
import com.shopease.shopease_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final CategoryRepository categoryRepository;

    public AdminService(ProductRepository productRepository,
                        OrderRepository orderRepository,
                        UserRepository userRepository,
                        PaymentRepository paymentRepository,
                        CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.categoryRepository = categoryRepository;
    }

    // Dashboard stats
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();

        // Total revenue from successful payments
        BigDecimal totalRevenue = paymentRepository.findAll().stream()
            .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
            .map(p -> p.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Orders by status
        long pendingOrders = orderRepository
            .findByStatus(OrderStatus.PENDING).size();
        long processingOrders = orderRepository
            .findByStatus(OrderStatus.PROCESSING).size();
        long deliveredOrders = orderRepository
            .findByStatus(OrderStatus.DELIVERED).size();
        long cancelledOrders = orderRepository
            .findByStatus(OrderStatus.CANCELLED).size();

        stats.put("totalUsers", totalUsers);
        stats.put("totalProducts", totalProducts);
        stats.put("totalOrders", totalOrders);
        stats.put("totalRevenue", totalRevenue);
        stats.put("pendingOrders", pendingOrders);
        stats.put("processingOrders", processingOrders);
        stats.put("deliveredOrders", deliveredOrders);
        stats.put("cancelledOrders", cancelledOrders);

        return stats;
    }

    // Get all users
    public List<Map<String, Object>> getAllUsers() {
        return userRepository.findAll().stream()
            .map(user -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("name", user.getName());
                map.put("email", user.getEmail());
                map.put("role", user.getRole());
                map.put("provider", user.getProvider());
                map.put("createdAt", user.getCreatedAt());
                return map;
            })
            .collect(Collectors.toList());
    }

    // Update user role
    @Transactional
    public Map<String, Object> updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setRole(Role.valueOf(role.toUpperCase()));
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User role updated to " + role);
        response.put("userId", userId);
        response.put("newRole", role);
        return response;
    }

    // Get all orders with user info
    public List<Map<String, Object>> getAllOrdersWithDetails() {
        return orderRepository.findAll().stream()
            .map(order -> {
                Map<String, Object> map = new HashMap<>();
                map.put("orderId", order.getId());
                map.put("userName", order.getUser().getName());
                map.put("userEmail", order.getUser().getEmail());
                map.put("totalAmount", order.getTotalAmount());
                map.put("status", order.getStatus());
                map.put("itemCount", order.getOrderItems().size());
                map.put("createdAt", order.getCreatedAt());
                return map;
            })
            .collect(Collectors.toList());
    }

    // Update order status
    @Transactional
    public Map<String, Object> updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
        order.setStatus(OrderStatus.valueOf(status.toUpperCase()));
        orderRepository.save(order);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order status updated to " + status);
        response.put("orderId", orderId);
        response.put("newStatus", status);
        return response;
    }
}