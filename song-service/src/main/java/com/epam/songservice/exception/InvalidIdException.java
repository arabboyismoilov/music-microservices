package com.epam.songservice.exception;

public class InvalidIdException extends RuntimeException {

    public InvalidIdException(String message) {
        super(message);
    }
}