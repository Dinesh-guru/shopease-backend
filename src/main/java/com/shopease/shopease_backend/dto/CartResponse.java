package com.shopease.shopease_backend.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartResponse {

    private List<CartItemResponse> items;
    private int totalItems;
    private BigDecimal totalAmount;

    public CartResponse(List<CartItemResponse> items) {
        this.items = items;
        this.totalItems = items.stream()
            .mapToInt(CartItemResponse::getQuantity)
            .sum();
        this.totalAmount = items.stream()
            .map(CartItemResponse::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Getters
    public List<CartItemResponse> getItems() { return items; }
    public int getTotalItems() { return totalItems; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}