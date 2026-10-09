package com.epam.resourceservice.dto;

public record ExtractedSongMetadata(
        String name,
        String artist,
        String album,
        String duration,
        String year
) {
}
