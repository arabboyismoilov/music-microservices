package com.epam.resourceservice.exception;

public class InvalidResourceIdException extends RuntimeException {
    public InvalidResourceIdException(String message) {
        super(message);
    }
}