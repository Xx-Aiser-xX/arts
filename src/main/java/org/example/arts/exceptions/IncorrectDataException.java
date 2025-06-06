package org.example.arts.exceptions;

public class IncorrectDataException extends RuntimeException{
    public IncorrectDataException(String message){
        super("Некорректные данные: " + message);
    }
}
