package org.example.arts.exceptions;

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        super("Некорректный пароль");
    }
}
