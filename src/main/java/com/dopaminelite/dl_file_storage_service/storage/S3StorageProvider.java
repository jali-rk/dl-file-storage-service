package com.dopaminelite.dl_file_storage_service.storage;

import com.dopaminelite.dl_file_storage_service.constant.SignedUrlIntent;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;

@Slf4j
public class S3StorageProvider implements StorageProvider {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;

    public S3StorageProvider(S3Client s3Client, S3Presigner s3Presigner, String bucketName) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        log.info("S3StorageProvider initialized with bucket: {}", bucketName);
    }

    @Override
    public String store(byte[] content, String storedFileName, String bucketPath) throws IOException {
        try {
            // Create S3 key in format: bucketPath/storedFileName
            String s3Key = bucketPath + "/" + storedFileName;

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));

            log.debug("Stored file '{}' in S3 bucket '{}' with key '{}'", storedFileName, bucketName, s3Key);
            return s3Key;
        } catch (Exception e) {
            log.error("Failed to store file '{}' in S3 bucket '{}'", storedFileName, bucketName, e);
            throw new IOException("Failed to store file in S3: " + e.getMessage(), e);
        }
    }

    @Override
    public String generateSignedUrl(String storagePath, SignedUrlIntent intent, int expiresInSeconds) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(storagePath)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofSeconds(expiresInSeconds))
                    .getObjectRequest(getObjectRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            String signedUrl = presignedRequest.url().toString();

            log.debug("Generated S3 signed URL for key='{}', intent: {}, expiresInSeconds: {}",
                    storagePath, intent, expiresInSeconds);
            return signedUrl;
        } catch (Exception e) {
            log.error("Failed to generate S3 signed URL for key='{}'", storagePath, e);
            throw new RuntimeException("Failed to generate signed URL: " + e.getMessage(), e);
        }
    }
}

