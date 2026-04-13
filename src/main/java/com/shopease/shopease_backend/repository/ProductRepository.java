package com.shopease.shopease_backend.repository;

import com.shopease.shopease_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Search by name (case-insensitive)
    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    // Filter by category
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    // Search by name within a category
    Page<Product> findByNameContainingIgnoreCaseAndCategoryId(
        String name, Long categoryId, Pageable pageable);
}