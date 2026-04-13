package com.shopease.shopease_backend.service;

import com.shopease.shopease_backend.dto.CartItemResponse;
import com.shopease.shopease_backend.dto.CartResponse;
import com.shopease.shopease_backend.entity.CartItem;
import com.shopease.shopease_backend.entity.Product;
import com.shopease.shopease_backend.entity.User;
import com.shopease.shopease_backend.repository.CartRepository;
import com.shopease.shopease_backend.repository.ProductRepository;
import com.shopease.shopease_backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.shopease.shopease_backend.exception.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository,
                       ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    // Get full cart for a user
    public CartResponse getCart(String email) {
        User user = getUserByEmail(email);
        List<CartItem> items = cartRepository.findByUserId(user.getId());
        List<CartItemResponse> responseItems = items.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
        return new CartResponse(responseItems);
    }

    // Add item to cart — if already exists, increase quantity
    @Transactional
    public CartResponse addToCart(String email, Long productId, Integer quantity) {
        User user = getUserByEmail(email);
        Product product = getProductById(productId);

        // Check stock availability
        if (product.getStockQuantity() < quantity) {
            throw new BadRequestException("Insufficient stock for: "
                + product.getName() + ". Available: " + product.getStockQuantity());
        }

        Optional<CartItem> existingItem =
            cartRepository.findByUserIdAndProductId(user.getId(), productId);

        if (existingItem.isPresent()) {
            // Product already in cart — just increase quantity
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + quantity;

            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: "
                    + product.getStockQuantity());
            }
            item.setQuantity(newQuantity);
            cartRepository.save(item);
        } else {
            // New item — add to cart
            CartItem newItem = new CartItem();
            newItem.setUser(user);
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cartRepository.save(newItem);
        }

        return getCart(email);
    }

    // Update quantity of a specific cart item
    @Transactional
    public CartResponse updateQuantity(String email, Long cartItemId, Integer quantity) {
        User user = getUserByEmail(email);
        CartItem item = cartRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Security check — make sure this cart item belongs to this user
        if (!item.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("This cart item doesn't belong to you");
        }

        if (quantity <= 0) {
            // If quantity is 0 or less, remove the item
            cartRepository.delete(item);
        } else {
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new RuntimeException("Insufficient stock. Available: "
                    + item.getProduct().getStockQuantity());
            }
            item.setQuantity(quantity);
            cartRepository.save(item);
        }

        return getCart(email);
    }

    // Remove a specific item from cart
    @Transactional
    public CartResponse removeFromCart(String email, Long cartItemId) {
        User user = getUserByEmail(email);
        CartItem item = cartRepository.findById(cartItemId)
            .orElseThrow(() -> new RuntimeException("Cart item not found"));

        // Security check
        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized — this item doesn't belong to you");
        }

        cartRepository.delete(item);
        return getCart(email);
    }

    // Clear entire cart
    @Transactional
    public void clearCart(String email) {
        User user = getUserByEmail(email);
        cartRepository.deleteByUserId(user.getId());
    }

    // Get cart item count — used for navbar badge
    public long getCartCount(String email) {
        User user = getUserByEmail(email);
        return cartRepository.countByUserId(user.getId());
    }

    // --- Private helper methods ---

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    private CartItemResponse toResponse(CartItem item) {
        return new CartItemResponse(
            item.getId(),
            item.getProduct().getId(),
            item.getProduct().getName(),
            item.getProduct().getImageUrl(),
            item.getProduct().getPrice(),
            item.getQuantity()
        );
    }
}