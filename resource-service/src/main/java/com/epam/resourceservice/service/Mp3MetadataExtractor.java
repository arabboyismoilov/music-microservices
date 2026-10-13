package com.epam.resourceservice.service;

import com.epam.resourceservice.dto.ExtractedSongMetadata;
import com.epam.resourceservice.exception.InvalidFileFormatException;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;

@Component
public class Mp3MetadataExtractor {

    private static final Logger log = LoggerFactory.getLogger(Mp3MetadataExtractor.class);

    public ExtractedSongMetadata extract(byte[] data) {
        if (data == null || data.length == 0) {
            throw new InvalidFileFormatException("File data is empty or invalid");
        }

        Metadata metadata = new Metadata();
        AutoDetectParser parser = new AutoDetectParser();

        try (TikaInputStream input = TikaInputStream.get(data)) {
            parser.parse(
                    input,
                    new BodyContentHandler(-1),
                    metadata,
                    new ParseContext()
            );
        } catch (TikaException | SAXException | IOException ex) {
            log.error("Failed to parse MP3 metadata: {}", ex.getMessage(), ex);
            throw new InvalidFileFormatException("Invalid MP3 file", ex);
        } catch (Exception ex) {
            log.error("Unexpected error during MP3 metadata extraction: {}", ex.getMessage(), ex);
            throw new InvalidFileFormatException("Invalid MP3 file", ex);
        }

        String name = metadata.get(TikaCoreProperties.TITLE);
        if (name == null || name.isBlank()) {
            name = metadata.get("title");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidFileFormatException("MP3 title metadata is missing");
        }

        String artist = metadata.get(XMPDM.ARTIST);
        if (artist == null || artist.isBlank()) {
            artist = metadata.get("artist");
        }
        if (artist == null || artist.isBlank()) {
            throw new InvalidFileFormatException("MP3 artist metadata is missing");
        }

        String album = metadata.get(XMPDM.ALBUM);
        if (album == null || album.isBlank()) {
            album = metadata.get("album");
        }
        if (album == null || album.isBlank()) {
            throw new InvalidFileFormatException("MP3 album metadata is missing");
        }

        String durationRaw = metadata.get(XMPDM.DURATION);
        if (durationRaw == null || durationRaw.isBlank()) {
            durationRaw = metadata.get("xmpDM:duration");
        }
        String duration = formatDuration(durationRaw);

        String releaseDate = metadata.get(XMPDM.RELEASE_DATE);
        if (releaseDate == null || releaseDate.isBlank()) {
            releaseDate = metadata.get("xmpDM:releaseDate");
        }
        String year = extractYear(releaseDate);

        return new ExtractedSongMetadata(
                name, artist, album, duration, year
        );
    }

    private String formatDuration(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidFileFormatException(
                    "MP3 duration metadata is missing"
            );
        }

        try {
            long totalSeconds = Math.round(Double.parseDouble(value));
            long minutes = totalSeconds / 60;
            long seconds = totalSeconds % 60;

            return String.format("%02d:%02d", minutes, seconds);
        } catch (NumberFormatException ex) {
            throw new InvalidFileFormatException(
                    "MP3 duration metadata is invalid"
            );
        }
    }

    private String extractYear(String releaseDate) {
        if (releaseDate == null || releaseDate.isBlank()) {
            throw new InvalidFileFormatException(
                    "MP3 release year metadata is missing"
            );
        }

        return releaseDate;
    }
}
