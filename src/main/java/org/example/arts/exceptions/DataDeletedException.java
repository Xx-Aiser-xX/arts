package org.example.arts.exceptions;

public class DataDeletedException extends RuntimeException{
    public DataDeletedException(String message){
        super("Данные удалены: " + message);
    }
}
