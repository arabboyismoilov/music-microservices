package com.epam.resourceprocessor.dto;

public record ExtractedSongMetadata(
        String name,
        String artist,
        String album,
        String duration,
        String year
) {
}
