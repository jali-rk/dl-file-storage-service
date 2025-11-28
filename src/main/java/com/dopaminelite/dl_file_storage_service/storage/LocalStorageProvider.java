package com.dopaminelite.dl_file_storage_service.storage;

import com.dopaminelite.dl_file_storage_service.constant.SignedUrlIntent;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
public class LocalStorageProvider implements StorageProvider {

    private final Path basePath;

    public LocalStorageProvider(String basePath) {
        this.basePath = Path.of(basePath).toAbsolutePath();
        log.debug("LocalStorageProvider initialized with basePath: {}", this.basePath);
    }

    @Override
    public String store(byte[] content, String storedFileName, String bucketPath) throws IOException {
        Path bucket = basePath.resolve(bucketPath);
        try {
            Files.createDirectories(bucket);
            Path filePath = bucket.resolve(storedFileName);
            Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.debug("Stored file '{}' at '{}'", storedFileName, filePath);
            return filePath.toString();
        } catch (IOException e) {
            log.error("Failed to store file '{}' in bucket '{}'", storedFileName, bucketPath, e);
            throw e;
        }
    }

    @Override
    public String generateSignedUrl(String storagePath, SignedUrlIntent intent, int expiresInSeconds) {
        try {
            String token = UUID.randomUUID().toString();
            OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(expiresInSeconds);
            String signedUrl = "http://localhost:8080/api/v1/files/raw?path=" + storagePath +
                    "&token=" + token +
                    "&exp=" + expiresAt.toEpochSecond() +
                    "&intent=" + intent.name();
            log.debug("Generated signed URL for storagePath='{}', intent: {}, expiresInSeconds: {}", storagePath, intent, expiresInSeconds);
            return signedUrl;
        } catch (Exception e) {
            log.error("Failed to generate signed URL for storagePath='{}'", storagePath, e);
            throw e;
        }
    }
}
