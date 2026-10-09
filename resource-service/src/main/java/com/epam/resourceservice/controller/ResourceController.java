package com.epam.resourceservice.controller;

import com.epam.resourceservice.service.IdParser;
import com.epam.resourceservice.service.ResourceService;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/resources")
public class ResourceController {

    private final ResourceService resourceService;
    private final IdParser idParser;

    public ResourceController(
            ResourceService resourceService,
            IdParser idParser
    ) {
        this.resourceService = resourceService;
        this.idParser = idParser;
    }

    @PostMapping
    public ResponseEntity<Map<String, Long>> upload(
            @RequestBody byte[] data,
            @RequestHeader(
                    value = "Content-Type",
                    required = false
            ) String contentType
    ) throws IOException, TikaException, SAXException {

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
        List<Long> ids = idParser.parse(id);
        List<Long> deletedIds = resourceService.deleteByIds(ids);

        return ResponseEntity.ok(Map.of("ids", deletedIds));
    }

}
