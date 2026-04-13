package com.shopease.shopease_backend.service;

import com.shopease.shopease_backend.dto.OrderItemResponse;
import com.shopease.shopease_backend.dto.OrderResponse;
import com.shopease.shopease_backend.entity.*;
import com.shopease.shopease_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shopease.shopease_backend.exception.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Place order from current cart
    // @Transactional means: if ANYTHING fails, ALL changes roll back
    @Transactional
    public OrderResponse placeOrder(String email) {
        User user = getUserByEmail(email);

        // Step 1 — Get all cart items
        List<CartItem> cartItems = cartRepository.findByUserId(user.getId());

        if (cartItems.isEmpty()) {
            throw new BadRequestException("Cart is empty. Add products before placing order.");
        }

        // Step 2 — Validate stock for ALL items before doing anything
        // This prevents partial failures
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
            	throw new BadRequestException("Insufficient stock for: "
            		    + product.getName() + ". Available: " + product.getStockQuantity());
            }
        }

        // Step 3 — Calculate total amount
        BigDecimal totalAmount = cartItems.stream()
            .map(item -> item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Step 4 — Create the order
        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);
        Order savedOrder = orderRepository.save(order);

        // Step 5 — Create order items + decrement stock
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            // Save order item with price snapshot
            // We save the price NOW because product price may change in future
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(product.getPrice()); // price snapshot
            savedOrder.getOrderItems().add(orderItem);

            // Decrement stock
            product.setStockQuantity(
                product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);
        }

        orderRepository.save(savedOrder);

        // Step 6 — Clear the cart
        cartRepository.deleteByUserId(user.getId());

        return toOrderResponse(savedOrder);
    }

    // Get all orders for logged-in user
    public List<OrderResponse> getMyOrders(String email) {
        User user = getUserByEmail(email);
        List<Order> orders = orderRepository
            .findByUserIdOrderByCreatedAtDesc(user.getId());
        return orders.stream()
            .map(this::toOrderResponse)
            .collect(Collectors.toList());
    }

    // Get single order by ID
    public OrderResponse getOrderById(String email, Long orderId) {
        User user = getUserByEmail(email);
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Security check — user can only view their own orders
        if (!order.getUser().getId().equals(user.getId())) {
        	throw new UnauthorizedException("This order doesn't belong to you");
        }

        return toOrderResponse(order);
    }

    // Cancel order — only allowed if status is PENDING
    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {
        User user = getUserByEmail(email);
        Order order = orderRepository.findById(orderId)
        		.orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        // Security check
        if (!order.getUser().getId().equals(user.getId())) {
        	throw new UnauthorizedException("This order doesn't belong to you");
        }

        // Can only cancel PENDING orders
        if (order.getStatus() != OrderStatus.PENDING) {
        	throw new BadRequestException("Cannot cancel order with status: "
        		    + order.getStatus() + ". Only PENDING orders can be cancelled.");
        }

        // Restore stock for each item
        for (OrderItem orderItem : order.getOrderItems()) {
            Product product = orderItem.getProduct();
            product.setStockQuantity(
                product.getStockQuantity() + orderItem.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        return toOrderResponse(order);
    }

    // Admin — update order status
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
        		.orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.setStatus(newStatus);
        orderRepository.save(order);
        return toOrderResponse(order);
    }

    // Admin — get all orders
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(this::toOrderResponse)
            .collect(Collectors.toList());
    }

    // --- Private helpers ---

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
        		.orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private OrderResponse toOrderResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems().stream()
            .map(item -> new OrderItemResponse(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getImageUrl(),
                item.getQuantity(),
                item.getPrice()
            ))
            .collect(Collectors.toList());

        return new OrderResponse(
            order.getId(),
            order.getTotalAmount(),
            order.getStatus(),
            order.getCreatedAt(),
            itemResponses
        );
    }
}