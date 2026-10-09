package com.epam.resourceservice.dto;

public record SongCreateRequest(
        Long id,
        String name,
        String artist,
        String album,
        String duration,
        String year
) {
}