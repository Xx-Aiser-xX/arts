package org.example.arts.exceptions;

public record ErrorResponse(
        int status,
        String error,
        String message,
        long timestamp
){
    public ErrorResponse(int status, String error, String message) {
        this(status, error, message, System.currentTimeMillis());
    }
}
