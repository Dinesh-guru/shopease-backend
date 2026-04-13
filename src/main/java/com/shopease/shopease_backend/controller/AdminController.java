package com.shopease.shopease_backend.controller;

import com.shopease.shopease_backend.dto.ProductRequest;
import com.shopease.shopease_backend.entity.Product;
import com.shopease.shopease_backend.service.AdminService;
import com.shopease.shopease_backend.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;
    private final ProductService productService;

    public AdminController(AdminService adminService,
                           ProductService productService) {
        this.adminService = adminService;
        this.productService = productService;
    }

    // GET /api/admin/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // GET /api/admin/users
    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    // PUT /api/admin/users/{id}/role?role=ADMIN
    @PutMapping("/users/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role) {
        return ResponseEntity.ok(adminService.updateUserRole(id, role));
    }

    // GET /api/admin/orders
    @GetMapping("/orders")
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        return ResponseEntity.ok(adminService.getAllOrdersWithDetails());
    }

    // PUT /api/admin/orders/{id}/status?status=SHIPPED
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(adminService.updateOrderStatus(id, status));
    }

    // POST /api/admin/products — create product
    @PostMapping("/products")
    public ResponseEntity<Product> createProduct(
            @RequestBody ProductRequest request) {
        return ResponseEntity.status(201)
            .body(productService.createProduct(request));
    }

    // PUT /api/admin/products/{id} — update product
    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // DELETE /api/admin/products/{id} — delete product
    @DeleteMapping("/products/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }
}