package com.epam.resourceservice.exception;

public class InvalidFileFormatException extends RuntimeException {
    public InvalidFileFormatException(String message) {
        super(message);
    }
}