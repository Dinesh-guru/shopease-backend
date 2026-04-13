package com.shopease.shopease_backend.controller;

import com.shopease.shopease_backend.dto.CartResponse;
import com.shopease.shopease_backend.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // GET /api/cart — view my cart
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(cartService.getCart(userDetails.getUsername()));
    }

    // POST /api/cart/add?productId=1&quantity=2
    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity) {
        return ResponseEntity.ok(
            cartService.addToCart(userDetails.getUsername(), productId, quantity));
    }

    // PUT /api/cart/update/{cartItemId}?quantity=3
    @PutMapping("/update/{cartItemId}")
    public ResponseEntity<CartResponse> updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(
            cartService.updateQuantity(userDetails.getUsername(), cartItemId, quantity));
    }

    // DELETE /api/cart/remove/{cartItemId}
    @DeleteMapping("/remove/{cartItemId}")
    public ResponseEntity<CartResponse> removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        return ResponseEntity.ok(
            cartService.removeFromCart(userDetails.getUsername(), cartItemId));
    }

    // DELETE /api/cart/clear
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        cartService.clearCart(userDetails.getUsername());
        return ResponseEntity.ok("Cart cleared successfully");
    }

    // GET /api/cart/count — for navbar badge
    @GetMapping("/count")
    public ResponseEntity<Long> getCartCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
            cartService.getCartCount(userDetails.getUsername()));
    }
}