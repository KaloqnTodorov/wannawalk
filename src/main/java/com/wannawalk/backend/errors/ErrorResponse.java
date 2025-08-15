package com.wannawalk.backend.errors;

// You can use Lombok's @Data or create getters manually.
public class ErrorResponse {
    private int status;
    private String message;
    // You could also add a timestamp, error code, etc.

    public ErrorResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    // Getters
    public int getStatus() { return status; }
    public String getMessage() { return message; }
}