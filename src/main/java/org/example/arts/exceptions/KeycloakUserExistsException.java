package org.example.arts.exceptions;

public class KeycloakUserExistsException extends RuntimeException {
    public KeycloakUserExistsException(String message) {
        super(message);
    }
    public KeycloakUserExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
