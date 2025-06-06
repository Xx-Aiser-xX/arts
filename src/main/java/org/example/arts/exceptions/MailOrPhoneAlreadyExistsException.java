package org.example.arts.exceptions;

public class MailOrPhoneAlreadyExistsException extends RuntimeException{
    public MailOrPhoneAlreadyExistsException(String message){
        super("Пользователь с " + message + " уже существует");
    }
}
