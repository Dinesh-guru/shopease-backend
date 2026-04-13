package com.shopease.shopease_backend.dto;

import com.shopease.shopease_backend.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long paymentId;
    private String transactionId;
    private Long orderId;
    private BigDecimal amount;
    private PaymentStatus status;
    private String paymentMethod;
    private String message;
    private LocalDateTime createdAt;

    public PaymentResponse(Long paymentId, String transactionId,
                            Long orderId, BigDecimal amount,
                            PaymentStatus status, String paymentMethod,
                            String message, LocalDateTime createdAt) {
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.orderId = orderId;
        this.amount = amount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.message = message;
        this.createdAt = createdAt;
    }

    // Getters
    public Long getPaymentId() { return paymentId; }
    public String getTransactionId() { return transactionId; }
    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getMessage() { return message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}