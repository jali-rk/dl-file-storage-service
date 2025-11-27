package com.dopaminelite.dl_file_storage_service.dto;

import com.dopaminelite.dl_file_storage_service.constant.FileContextType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFileDto {
    private UUID id;
    private String originalFileName;
    private String storedFileName;
    private String mimeType;
    private long sizeBytes;
    private String sha256;
    private String bucket;
    private String storagePath;
    private FileContextType contextType;
    private String contextRefId;
    private UUID createdByUserId;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean isDeleted;
}
