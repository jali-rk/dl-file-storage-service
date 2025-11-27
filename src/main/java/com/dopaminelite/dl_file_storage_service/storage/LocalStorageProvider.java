package com.dopaminelite.dl_file_storage_service.storage;

import com.dopaminelite.dl_file_storage_service.constant.SignedUrlIntent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.UUID;

public class LocalStorageProvider implements StorageProvider {

    private final Path basePath;

    public LocalStorageProvider(String basePath) {
        this.basePath = Path.of(basePath).toAbsolutePath();
    }

    @Override
    public String store(byte[] content, String storedFileName, String bucketPath) throws IOException {
        Path bucket = basePath.resolve(bucketPath);
        Files.createDirectories(bucket);
        Path filePath = bucket.resolve(storedFileName);
        Files.write(filePath, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return filePath.toString();
    }

    @Override
    public String generateSignedUrl(String storagePath, SignedUrlIntent intent, int expiresInSeconds) {
        // Simplistic placeholder signed URL generation for local storage.
        String token = UUID.randomUUID().toString();
        OffsetDateTime expiresAt = OffsetDateTime.now().plusSeconds(expiresInSeconds);
        // In a real implementation you'd persist token and validate later.
        return "http://localhost:8080/api/v1/files/raw?path=" + storagePath + "&token=" + token + "&exp=" + expiresAt.toEpochSecond() + "&intent=" + intent.name();
    }
}

