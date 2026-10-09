package com.epam.resourceservice.service;

import com.epam.resourceservice.dto.ExtractedSongMetadata;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.metadata.XMPDM;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.io.TikaInputStream;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;

@Component
public class Mp3MetadataExtractor {

    public ExtractedSongMetadata extract(byte[] data)
            throws IOException, TikaException, SAXException {

        Metadata metadata = new Metadata();
        AutoDetectParser parser = new AutoDetectParser();

        try (TikaInputStream input = TikaInputStream.get(data)) {
            parser.parse(
                    input,
                    new BodyContentHandler(-1),
                    metadata,
                    new ParseContext()
            );
        }

        String name = metadata.get(TikaCoreProperties.TITLE);
        String artist = metadata.get(XMPDM.ARTIST);
        String album = metadata.get(XMPDM.ALBUM);
        String duration = formatDuration(metadata.get(XMPDM.DURATION));
        String year = extractYear(metadata.get(XMPDM.RELEASE_DATE));

        return new ExtractedSongMetadata(
                name, artist, album, duration, year
        );
    }

    private String formatDuration(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "MP3 duration metadata is missing"
            );
        }

        long totalSeconds = Math.round(Double.parseDouble(value));
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    private String extractYear(String releaseDate) {
        if (releaseDate == null || releaseDate.length() < 4) {
            throw new IllegalArgumentException(
                    "MP3 release year metadata is missing"
            );
        }

        return releaseDate.substring(0, 4);
    }
}
