package com.epam.songservice.exception;

public class DuplicateSongException extends RuntimeException {

    public DuplicateSongException(Long id) {
        super("Metadata for resource ID=" + id + " already exists");
    }
}