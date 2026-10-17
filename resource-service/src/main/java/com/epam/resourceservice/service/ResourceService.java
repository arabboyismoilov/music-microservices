package com.epam.resourceservice.service;

import com.epam.resourceservice.entity.Resource;
import com.epam.resourceservice.exception.InvalidFileFormatException;
import com.epam.resourceservice.exception.InvalidResourceIdException;
import com.epam.resourceservice.exception.ResourceNotFoundException;
import com.epam.resourceservice.exception.SongServiceException;
import com.epam.resourceservice.repository.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);

    private final ResourceRepository resourceRepository;
    private final S3StorageService s3StorageService;
    private final RestClient songServiceRestClient;
    private final IdParser idParser;

    public ResourceService(
            ResourceRepository resourceRepository,
            S3StorageService s3StorageService,
            RestClient songServiceRestClient,
            IdParser idParser
    ) {
        this.resourceRepository = resourceRepository;
        this.s3StorageService = s3StorageService;
        this.songServiceRestClient = songServiceRestClient;
        this.idParser = idParser;
    }

    @Transactional
    public Long upload(byte[] data, String contentType) {
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

        if (data == null || data.length == 0) {
            throw new InvalidFileFormatException("File data is empty or invalid");
        }

        String location = "resources/" + UUID.randomUUID() + ".mp3";
        s3StorageService.upload(location, data, contentType);

        Resource resource = resourceRepository.save(new Resource(location));
        return resource.getId();
    }

    @Transactional(readOnly = true)
    public byte[] getById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidResourceIdException(
                    String.format("Invalid value '%s' for ID. Must be a positive integer", id)
            );
        }

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        return s3StorageService.download(resource.getLocation());
    }

    @Transactional
    public List<Long> deleteByIds(String csv) {
        List<Long> ids = idParser.parse(csv);
        return deleteByIds(ids);
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

        for (Resource resource : resources) {
            try {
                s3StorageService.delete(resource.getLocation());
            } catch (Exception ex) {
                log.warn("Failed to delete S3 object '{}': {}", resource.getLocation(), ex.getMessage());
            }
        }

        resourceRepository.deleteAll(resources);

        try {
            songServiceRestClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path("/songs")
                            .queryParam("id", deletedIds.stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(",")))
                            .build())
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            log.error("Failed to delete song metadata from Song Service: {}", ex.getMessage(), ex);
            throw new SongServiceException(ex);
        }

        return deletedIds;
    }
}