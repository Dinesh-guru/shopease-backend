package com.shopease.shopease_backend.dto;

import java.time.LocalDateTime;
import java.util.Map;

public class ErrorResponse {

    private boolean success = false;
    private int status;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> validationErrors; // for field-level errors

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String message,
                          Map<String, String> validationErrors) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.validationErrors = validationErrors;
    }

    // Getters
    public boolean isSuccess() { return success; }
    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public Map<String, String> getValidationErrors() { return validationErrors; }
}