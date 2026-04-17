package com.shopease.shopease_backend.controller;



import com.shopease.shopease_backend.dto.PaymentRequest;
import com.shopease.shopease_backend.dto.PaymentResponse;
import com.shopease.shopease_backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // POST /api/payments/process
    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(
            paymentService.processPayment(
                userDetails.getUsername(), request));
    }

    // GET /api/payments/order/{orderId}
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        return ResponseEntity.ok(
            paymentService.getPaymentByOrderId(
                userDetails.getUsername(), orderId));
    }

    // POST /api/payments/retry
    @PostMapping("/retry")
    public ResponseEntity<PaymentResponse> retryPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(
            paymentService.retryPayment(
                userDetails.getUsername(), request));
    }
}