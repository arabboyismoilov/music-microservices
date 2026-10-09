package com.epam.songservice.controller;

import com.epam.songservice.dto.SongRequest;
import com.epam.songservice.dto.SongResponse;
import com.epam.songservice.service.IdParser;
import com.epam.songservice.service.SongService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
    private final IdParser idParser;

    public SongController(
            SongService songService,
            IdParser idParser
    ) {
        this.songService = songService;
        this.idParser = idParser;
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
        List<Long> ids = idParser.parse(id);
        List<Long> deletedIds = songService.deleteByIds(ids);

        return ResponseEntity.ok(Map.of("ids", deletedIds));
    }
}