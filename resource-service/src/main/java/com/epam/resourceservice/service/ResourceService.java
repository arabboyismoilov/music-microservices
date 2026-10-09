package com.epam.resourceservice.service;

import com.epam.resourceservice.dto.ExtractedSongMetadata;
import com.epam.resourceservice.dto.SongCreateRequest;
import com.epam.resourceservice.entity.Resource;
import com.epam.resourceservice.exception.InvalidFileFormatException;
import com.epam.resourceservice.exception.InvalidResourceIdException;
import com.epam.resourceservice.exception.ResourceNotFoundException;
import com.epam.resourceservice.exception.SongServiceException;
import com.epam.resourceservice.repository.ResourceRepository;
import org.apache.tika.exception.TikaException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

@Service
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final Mp3MetadataExtractor metadataExtractor;
    private final RestClient songServiceRestClient;

    public ResourceService(
            ResourceRepository resourceRepository,
            Mp3MetadataExtractor metadataExtractor,
            RestClient songServiceRestClient
    ) {
        this.resourceRepository = resourceRepository;
        this.metadataExtractor = metadataExtractor;
        this.songServiceRestClient = songServiceRestClient;
    }

    @Transactional
    public Long upload(byte[] data, String contentType)
            throws IOException, TikaException, SAXException {


        if (contentType == null) {
            throw new InvalidFileFormatException(
                    "Content-Type is required. Only MP3 files are allowed"
            );
        }

        try {
            MediaType mediaType = MediaType.parseMediaType(contentType);

            if (!"audio".equalsIgnoreCase(mediaType.getType())
                    || !"mpeg".equalsIgnoreCase(mediaType.getSubtype())) {
                throw new InvalidFileFormatException(
                        "Invalid file format: " + contentType
                                + ". Only MP3 files are allowed"
                );
            }
        } catch (org.springframework.http.InvalidMediaTypeException ex) {
            throw new InvalidFileFormatException(
                    "Invalid file format: " + contentType
                            + ". Only MP3 files are allowed"
            );
        }

        ExtractedSongMetadata metadata = metadataExtractor.extract(data);

        Resource resource = resourceRepository.save(new Resource(data));

        SongCreateRequest songRequest = new SongCreateRequest(
                resource.getId(),
                metadata.name(),
                metadata.artist(),
                metadata.album(),
                metadata.duration(),
                metadata.year()
        );


        try {
            songServiceRestClient.post()
                    .uri("/songs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(songRequest)
                    .retrieve()
                    .toBodilessEntity();
        } catch (org.springframework.web.client.RestClientException ex) {
            resourceRepository.delete(resource);
            throw new SongServiceException();
        }

        return resource.getId();
    }

    @Transactional(readOnly = true)
    public byte[] getById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidResourceIdException(String.format("Invalid value '%s' for ID. Must be a positive integer", id));
        }

        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id))
                .getData();
    }


    @Transactional
    public List<Long> deleteByIds(List<Long> ids) {
        List<Resource> resources = resourceRepository.findAllById(ids);

        List<Long> deletedIds = resources.stream()
                .map(Resource::getId)
                .toList();

        if (deletedIds.isEmpty()) {
            return List.of();
        }

        try {
            songServiceRestClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/songs")
                            .queryParam("id", deletedIds.stream()
                                    .map(String::valueOf)
                                    .collect(java.util.stream.Collectors.joining(",")))
                            .build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (org.springframework.web.client.RestClientException ex) {
            throw new SongServiceException();
        }

        resourceRepository.deleteAllById(deletedIds);

        return deletedIds;
    }

}