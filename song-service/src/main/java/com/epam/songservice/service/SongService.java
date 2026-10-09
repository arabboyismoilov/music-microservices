package com.epam.songservice.service;

import com.epam.songservice.dto.SongRequest;
import com.epam.songservice.dto.SongResponse;
import com.epam.songservice.entity.Song;
import com.epam.songservice.exception.DuplicateSongException;
import com.epam.songservice.exception.InvalidIdException;
import com.epam.songservice.exception.SongNotFoundException;
import com.epam.songservice.repository.SongRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SongService {

    private final SongRepository songRepository;

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    @Transactional
    public SongResponse create(SongRequest request) {

        if (songRepository.existsById(request.id())) {
            throw new DuplicateSongException(request.id());
        }

        Song song = new Song(
                request.id(),
                request.name(),
                request.artist(),
                request.album(),
                request.duration(),
                request.year()
        );

        Song savedSong = songRepository.save(song);

        return toResponse(savedSong);
    }

    public SongResponse getById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidIdException(
                    String.format("Invalid value '%s' for ID. Must be a positive integer", id)
            );
        }

        Song song = songRepository.findById(id)
                .orElseThrow(() -> new SongNotFoundException(id));

        return toResponse(song);
    }

    @Transactional
    public List<Long> deleteByIds(List<Long> ids) {

        List<Song> existingSongs = songRepository.findAllById(ids);

        List<Long> deletedIds = existingSongs.stream()
                .map(Song::getId)
                .toList();

        songRepository.deleteAllById(deletedIds);

        return deletedIds;
    }

    private SongResponse toResponse(Song song) {
        return new SongResponse(
                song.getId(),
                song.getName(),
                song.getArtist(),
                song.getAlbum(),
                song.getDuration(),
                song.getYear()
        );
    }
}