package com.dopaminelite.dl_file_storage_service.dto;

import com.dopaminelite.dl_file_storage_service.entity.FileContextType;
import lombok.*;

import java.time.OffsetDateTime;
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
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private boolean isDeleted;
}
