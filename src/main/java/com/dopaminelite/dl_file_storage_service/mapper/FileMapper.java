package com.dopaminelite.dl_file_storage_service.mapper;

import com.dopaminelite.dl_file_storage_service.dto.StoredFileDto;
import com.dopaminelite.dl_file_storage_service.entity.StoredFile;

public class FileMapper {

    public static StoredFileDto toDto(StoredFile entity) {
        if (entity == null) return null;
        return StoredFileDto.builder()
                .id(entity.getId())
                .originalFileName(entity.getOriginalFileName())
                .storedFileName(entity.getStoredFileName())
                .mimeType(entity.getMimeType())
                .sizeBytes(entity.getSizeBytes())
                .sha256(entity.getSha256())
                .bucket(entity.getBucket())
                .storagePath(entity.getStoragePath())
                .contextType(entity.getContextType())
                .contextRefId(entity.getContextRefId())
                .createdByUserId(entity.getCreatedByUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .isDeleted(entity.isDeleted())
                .build();
    }
}

