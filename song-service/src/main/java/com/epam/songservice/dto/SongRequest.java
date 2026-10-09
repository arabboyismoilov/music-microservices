package com.epam.songservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SongRequest(

        @NotNull(message = "ID is required")
        @Positive(message = "ID must be a positive number")
        Long id,

        @NotBlank(message = "Song name is required")
        @Size(min = 1, max = 100,
                message = "Song name must be between 1 and 100 characters")
        String name,

        @NotBlank(message = "Artist name is required")
        @Size(min = 1, max = 100,
                message = "Artist name must be between 1 and 100 characters")
        String artist,

        @NotBlank(message = "Album name is required")
        @Size(min = 1, max = 100,
                message = "Album name must be between 1 and 100 characters")
        String album,

        @NotBlank(message = "Duration is required")
        @Pattern(
                regexp = "^\\d{2}:[0-5]\\d$",
                message = "Duration must be in mm:ss format with leading zeros"
        )
        String duration,

        @NotBlank(message = "Year is required")
        @Pattern(
                regexp = "^(19\\d{2}|20\\d{2})$",
                message = "Year must be between 1900 and 2099"
        )
        String year
) {}