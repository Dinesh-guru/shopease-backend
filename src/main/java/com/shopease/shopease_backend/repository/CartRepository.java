package com.shopease.shopease_backend.repository;

import com.shopease.shopease_backend.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<CartItem, Long> {

    // Get all cart items for a user
    List<CartItem> findByUserId(Long userId);

    // Find a specific product in a user's cart
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    // Delete all items in a user's cart (used after order placement)
    void deleteByUserId(Long userId);

    // Count items in cart (for navbar badge)
    long countByUserId(Long userId);
}