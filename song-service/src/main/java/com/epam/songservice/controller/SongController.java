package com.epam.songservice.controller;

import com.epam.songservice.dto.SongRequest;
import com.epam.songservice.dto.SongResponse;
import com.epam.songservice.service.SongService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songs")
@Validated
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> create(
            @Valid @RequestBody SongRequest request
    ) {
        SongResponse response = songService.create(request);

        return ResponseEntity.ok(Map.of("id", response.id()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SongResponse> getById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(songService.getById(id));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> delete(
            @RequestParam String id
    ) {
        List<Long> deletedIds = songService.deleteByIds(id);

        return ResponseEntity.ok(Map.of("ids", deletedIds));
    }
}