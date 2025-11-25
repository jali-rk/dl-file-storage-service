package com.dopaminelite.dl_file_storage_service.storage;

import com.dopaminelite.dl_file_storage_service.dto.SignedUrlIntent;
import java.io.IOException;

public interface StorageProvider {
    String store(byte[] content, String storedFileName, String bucketPath) throws IOException;
    String generateSignedUrl(String storagePath, SignedUrlIntent intent, int expiresInSeconds);
}

