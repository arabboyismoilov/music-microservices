package com.epam.resourceservice.exception;

public class SongServiceException extends RuntimeException {
    public SongServiceException() {
        super("Could not save the MP3 metadata in the Song Service");
    }
}