package com.shopease.shopease_backend.service;

import com.shopease.shopease_backend.dto.PaymentRequest;
import com.shopease.shopease_backend.dto.PaymentResponse;
import com.shopease.shopease_backend.entity.*;
import com.shopease.shopease_backend.exception.*;
import com.shopease.shopease_backend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.Random;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          OrderRepository orderRepository,
                          UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PaymentResponse processPayment(String email, PaymentRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Order", request.getOrderId()));

        // Security — user can only pay for their own orders
        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("This order doesn't belong to you");
        }

        // Check order is in PENDING status
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BadRequestException(
                "Order is not in PENDING status. Current status: "
                + order.getStatus());
        }

        // Check payment doesn't already exist for this order
        if (paymentRepository.findByOrderId(order.getId()).isPresent()) {
            throw new BadRequestException(
                "Payment already processed for this order");
        }

        // Generate unique transaction ID
        String transactionId = "TXN" + UUID.randomUUID()
            .toString().replace("-", "").substring(0, 12).toUpperCase();

        // Simulate payment processing
        // 90% success rate — realistic mock behaviour
        boolean paymentSuccess = simulatePayment(request);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentId(transactionId);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(request.getPaymentMethod());

        if (paymentSuccess) {
            payment.setStatus(PaymentStatus.SUCCESS);
            // Update order status to PROCESSING
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            // Keep order as PENDING so user can retry
        }

        Payment savedPayment = paymentRepository.save(payment);

        return new PaymentResponse(
            savedPayment.getId(),
            transactionId,
            order.getId(),
            savedPayment.getAmount(),
            savedPayment.getStatus(),
            savedPayment.getPaymentMethod(),
            paymentSuccess
                ? "Payment successful! Your order is being processed."
                : "Payment failed. Please try again.",
            savedPayment.getCreatedAt()
        );
    }

    // Get payment status for an order
    public PaymentResponse getPaymentByOrderId(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("This order doesn't belong to you");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No payment found for order: " + orderId));

        return new PaymentResponse(
            payment.getId(),
            payment.getPaymentId(),
            orderId,
            payment.getAmount(),
            payment.getStatus(),
            payment.getPaymentMethod(),
            payment.getStatus() == PaymentStatus.SUCCESS
                ? "Payment successful"
                : "Payment failed",
            payment.getCreatedAt()
        );
    }

    // Retry failed payment
    @Transactional
    public PaymentResponse retryPayment(String email, PaymentRequest request) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Order", request.getOrderId()));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("This order doesn't belong to you");
        }

        // Find existing failed payment
        Payment existingPayment = paymentRepository.findByOrderId(order.getId())
            .orElseThrow(() -> new ResourceNotFoundException(
                "No payment found for this order"));

        if (existingPayment.getStatus() != PaymentStatus.FAILED) {
            throw new BadRequestException(
                "Only failed payments can be retried");
        }

        // Generate new transaction ID
        String newTransactionId = "TXN" + UUID.randomUUID()
            .toString().replace("-", "").substring(0, 12).toUpperCase();

        boolean paymentSuccess = simulatePayment(request);

        existingPayment.setPaymentId(newTransactionId);
        existingPayment.setPaymentMethod(request.getPaymentMethod());

        if (paymentSuccess) {
            existingPayment.setStatus(PaymentStatus.SUCCESS);
            order.setStatus(OrderStatus.PROCESSING);
            orderRepository.save(order);
        }

        paymentRepository.save(existingPayment);

        return new PaymentResponse(
            existingPayment.getId(),
            newTransactionId,
            order.getId(),
            existingPayment.getAmount(),
            existingPayment.getStatus(),
            existingPayment.getPaymentMethod(),
            paymentSuccess
                ? "Payment successful! Your order is being processed."
                : "Payment failed again. Please try a different method.",
            LocalDateTime.now()
        );
    }

    // Simulate payment — 90% success rate
    private boolean simulatePayment(PaymentRequest request) {
        // Specific test card always fails — useful for testing failure flow
        if (request.getCardNumber() != null
                && request.getCardNumber().startsWith("0000")) {
            return false;
        }

        // Add small delay to simulate real payment processing
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 90% success rate
        return new Random().nextInt(100) < 90;
    }
}