package com.epam.resourceservice.controller;

import com.epam.resourceservice.service.ResourceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> upload(
            @RequestBody byte[] data,
            @RequestHeader(
                    value = "Content-Type",
                    required = false
            ) String contentType
    ) {
        Long id = resourceService.upload(data, contentType);

        return ResponseEntity.ok(Map.of("id", id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getById(@PathVariable Long id) {
        byte[] data = resourceService.getById(id);

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.valueOf("audio/mpeg"))
                .body(data);
    }

    @DeleteMapping
    public ResponseEntity<Map<String, List<Long>>> delete(
            @RequestParam String id
    ) {
        List<Long> deletedIds = resourceService.deleteByIds(id);

        return ResponseEntity.ok(Map.of("ids", deletedIds));
    }
}
