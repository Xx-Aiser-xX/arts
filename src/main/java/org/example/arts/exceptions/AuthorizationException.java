package org.example.arts.exceptions;

public class AuthorizationException extends RuntimeException{
    public AuthorizationException(){
        super("Пользоваетль не авторизирован");
    }
}