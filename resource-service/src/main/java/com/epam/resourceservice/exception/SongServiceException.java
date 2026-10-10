package com.epam.resourceservice.exception;

public class SongServiceException extends RuntimeException {

    public SongServiceException() {
        super("Could not save the MP3 metadata in the Song Service");
    }

    public SongServiceException(Throwable cause) {
        super("Could not save the MP3 metadata in the Song Service", cause);
    }

    public SongServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}