package com.digibank.restapi.exception;

public class TransferFailedException extends RuntimeException{
    public TransferFailedException(String message) {
        super(message);
    }
    public TransferFailedException(String message, Throwable cause) {
        super(message, cause);
    }

}
