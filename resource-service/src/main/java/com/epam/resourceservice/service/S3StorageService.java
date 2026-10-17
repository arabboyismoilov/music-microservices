package com.epam.resourceservice.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

@Service
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(
            S3Client s3Client,
            @Value("${aws.s3.bucket:resources}") String bucketName
    ) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @PostConstruct
    public void initBucket() {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
            log.info("S3 bucket '{}' already exists", bucketName);
        } catch (NoSuchBucketException ex) {
            log.info("Creating S3 bucket '{}'", bucketName);
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
        } catch (Exception ex) {
            log.warn("Could not check/create bucket '{}' on startup: {}", bucketName, ex.getMessage());
        }
    }

    public void upload(String key, byte[] data, String contentType) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .contentLength((long) data.length)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(data));
        log.info("Uploaded object to S3 with key '{}'", key);
    }

    public byte[] download(String key) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObjectAsBytes(getRequest).asByteArray();
    }

    public void delete(String key) {
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3Client.deleteObject(deleteRequest);
        log.info("Deleted object from S3 with key '{}'", key);
    }

    public String getBucketName() {
        return bucketName;
    }
}
