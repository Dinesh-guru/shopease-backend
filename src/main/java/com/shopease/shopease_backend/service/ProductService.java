package com.shopease.shopease_backend.service;

import com.shopease.shopease_backend.dto.ProductRequest;
import com.shopease.shopease_backend.entity.Category;
import com.shopease.shopease_backend.entity.Product;
import com.shopease.shopease_backend.repository.CategoryRepository;
import com.shopease.shopease_backend.repository.ProductRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import com.shopease.shopease_backend.exception.ResourceNotFoundException;
import com.shopease.shopease_backend.exception.BadRequestException;
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // Get all products with optional search and category filter
    public Page<Product> getProducts(int page, int size, String search, Long categoryId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        if (search != null && categoryId != null) {
            return productRepository
                .findByNameContainingIgnoreCaseAndCategoryId(search, categoryId, pageable);
        } else if (search != null) {
            return productRepository
                .findByNameContainingIgnoreCase(search, pageable);
        } else if (categoryId != null) {
            return productRepository
                .findByCategoryId(categoryId, pageable);
        } else {
            return productRepository.findAll(pageable);
        }
    }

    // Get single product by ID
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }
    // Create new product
    public Product createProduct(ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
        	Category category = categoryRepository.findById(request.getCategoryId())
        		    .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));
        }

        return productRepository.save(product);
    }

    // Update existing product
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = getProductById(id);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        return productRepository.save(product);
    }

    // Delete product
    public void deleteProduct(Long id) {
        Product product = getProductById(id); // throws if not found
        productRepository.delete(product);
    }
}