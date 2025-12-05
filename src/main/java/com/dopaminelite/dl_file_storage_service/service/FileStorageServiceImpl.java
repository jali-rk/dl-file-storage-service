package com.dopaminelite.dl_file_storage_service.service;

import com.dopaminelite.dl_file_storage_service.config.StorageProperties;
import com.dopaminelite.dl_file_storage_service.constant.FileContextType;
import com.dopaminelite.dl_file_storage_service.constant.SignedUrlIntent;
import com.dopaminelite.dl_file_storage_service.dto.*;
import com.dopaminelite.dl_file_storage_service.entity.StoredFile;
import com.dopaminelite.dl_file_storage_service.exception.BadRequestException;
import com.dopaminelite.dl_file_storage_service.exception.NotFoundException;
import com.dopaminelite.dl_file_storage_service.mapper.FileMapper;
import com.dopaminelite.dl_file_storage_service.repository.StoredFileRepository;
import com.dopaminelite.dl_file_storage_service.storage.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private final StoredFileRepository repository;
    private final StorageProvider storageProvider;
    private final StorageProperties storageProperties;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, UUID createdByUserId, FileContextType contextType, String contextRefId, boolean generateSignedUrl) {
        if (file == null || file.isEmpty()) {
            log.error("Attempted to upload empty file by userId: {}", createdByUserId);
            throw new BadRequestException("File must not be empty");
        }

        String originalName = file.getOriginalFilename();
        String mimeType = file.getContentType();
        long sizeBytes = file.getSize();
        String storedFileName = UUID.randomUUID() + "_" + originalName;
        String bucket = contextType.name().toLowerCase();

        log.debug("Uploading file '{}' (size: {} bytes, type: {}) for userId: {}, contextType: {}, contextRefId: {}",
                originalName, sizeBytes, mimeType, createdByUserId, contextType, contextRefId);

        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read file bytes for '{}'", originalName, e);
            throw new BadRequestException("Unable to read file bytes: " + e.getMessage());
        }

        String storagePath;
        try {
            storagePath = storageProvider.store(content, storedFileName, bucket);
        } catch (IOException e) {
            log.error("Failed to store file '{}' for userId: {}", originalName, createdByUserId, e);
            throw new BadRequestException("Failed to store file: " + e.getMessage());
        }

        StoredFile entity = StoredFile.builder()
                .originalFileName(originalName)
                .storedFileName(storedFileName)
                .mimeType(mimeType)
                .sizeBytes(sizeBytes)
                .bucket(bucket)
                .storagePath(storagePath)
                .contextType(contextType)
                .contextRefId(contextRefId)
                .createdByUserId(createdByUserId)
                .sha256(null)
                .isDeleted(false)
                .build();

        entity = repository.save(entity);
        log.debug("File '{}' saved in database with id: {}", originalName, entity.getId());

        StoredFileDto dto = FileMapper.toDto(entity);
        String signedUrl = null;
        OffsetDateTime expiresAt = null;

        if (generateSignedUrl) {
            int exp = storageProperties.getSignedUrl().getDefaultExpirationSeconds();
            signedUrl = storageProvider.generateSignedUrl(entity.getStoragePath(), SignedUrlIntent.VIEW, exp);
            expiresAt = OffsetDateTime.now().plusSeconds(exp);
            log.debug("Generated signed URL for fileId: {}", entity.getId());
        }

        return FileUploadResponse.builder()
                .file(dto)
                .signedUrl(signedUrl)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public StoredFileDto getFile(UUID fileId) {
        log.debug("Fetching file with id: {}", fileId);
        StoredFile entity = repository.findByIdAndIsDeletedFalse(fileId)
                .orElseThrow(() -> {
                    log.error("File not found with id: {}", fileId);
                    return new NotFoundException("File not found: " + fileId);
                });
        return FileMapper.toDto(entity);
    }

    @Override
    public void softDeleteFile(UUID fileId) {
        log.debug("Soft deleting file with id: {}", fileId);
        StoredFile entity = repository.findByIdAndIsDeletedFalse(fileId)
                .orElseThrow(() -> {
                    log.error("File not found for deletion: id: {}", fileId);
                    return new NotFoundException("File not found: " + fileId);
                });
        entity.setDeleted(true);
        repository.save(entity);
        log.debug("File with id: {} marked as deleted", fileId);
    }

    @Override
    @Transactional(readOnly = true)
    public FileSignedUrlResponse generateSignedUrl(UUID fileId, SignedUrlIntent intent, Integer expiresInSeconds) {
        log.debug("Generating signed URL for fileId: {}, intent: {}", fileId, intent);
        StoredFile entity = repository.findByIdAndIsDeletedFalse(fileId)
                .orElseThrow(() -> {
                    log.error("File not found for signed URL: id: {}", fileId);
                    return new NotFoundException("File not found: " + fileId);
                });
        int exp = expiresInSeconds != null ? expiresInSeconds : storageProperties.getSignedUrl().getDefaultExpirationSeconds();
        String url = storageProvider.generateSignedUrl(entity.getStoragePath(), intent, exp);
        log.debug("Signed URL generated for fileId: {}", fileId);
        return FileSignedUrlResponse.builder()
                .fileId(entity.getId())
                .url(url)
                .expiresAt(OffsetDateTime.now().plusSeconds(exp))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BulkFileSignedUrlResponse bulkGenerateSignedUrls(List<BulkFileSignedUrlRequestItem> items, Integer expiresInSeconds) {
        if (items == null || items.isEmpty()) {
            log.error("Empty items list for bulk signed URL generation");
            throw new BadRequestException("Items list must not be empty");
        }
        int exp = expiresInSeconds != null ? expiresInSeconds : storageProperties.getSignedUrl().getDefaultExpirationSeconds();
        List<BulkFileSignedUrlResponseItem> responses = new ArrayList<>();
        for (BulkFileSignedUrlRequestItem item : items) {
            StoredFile entity = repository.findByIdAndIsDeletedFalse(item.getFileId())
                    .orElseThrow(() -> {
                        log.error("File not found for bulk signed URL: id: {}", item.getFileId());
                        return new NotFoundException("File not found: " + item.getFileId());
                    });
            String url = storageProvider.generateSignedUrl(entity.getStoragePath(), item.getIntent() == null ? SignedUrlIntent.VIEW : item.getIntent(), exp);
            responses.add(BulkFileSignedUrlResponseItem.builder()
                    .fileId(entity.getId())
                    .url(url)
                    .expiresAt(OffsetDateTime.now().plusSeconds(exp))
                    .build());
            log.debug("Bulk signed URL generated for fileId: {}", entity.getId());
        }
        return BulkFileSignedUrlResponse.builder().items(responses).build();
    }

    @Override
    @Transactional(readOnly = true)
    public FileListResponse listFiles(UUID createdByUserId, FileContextType contextType, String contextRefId, int limit, int offset) {
        log.debug("Listing files with filters: userId: {}, contextType: {}, contextRefId: {}, limit: {}, offset: {}",
                createdByUserId, contextType, contextRefId, limit, offset);

        if (limit <= 0) throw new BadRequestException("limit must be > 0");
        if (limit > 100) throw new BadRequestException("limit must be <= 100");
        if (offset < 0) throw new BadRequestException("offset must be >= 0");

        int page = offset / limit;
        PageRequest pr = PageRequest.of(page, limit);
        Page<StoredFile> result;

        if (createdByUserId != null && contextType != null && contextRefId != null) {
            result = repository.findByCreatedByUserIdAndContextTypeAndContextRefIdAndIsDeletedFalse(createdByUserId, contextType, contextRefId, pr);
        } else if (createdByUserId != null && contextType != null) {
            result = repository.findByCreatedByUserIdAndContextTypeAndIsDeletedFalse(createdByUserId, contextType, pr);
        } else if (createdByUserId != null) {
            result = repository.findByCreatedByUserIdAndIsDeletedFalse(createdByUserId, pr);
        } else if (contextType != null && contextRefId != null) {
            result = repository.findByContextTypeAndContextRefIdAndIsDeletedFalse(contextType, contextRefId, pr);
        } else if (contextType != null) {
            result = repository.findByContextTypeAndIsDeletedFalse(contextType, pr);
        } else {
            result = repository.findByIsDeletedFalse(pr);
        }

        List<StoredFileDto> items = result.getContent().stream().map(FileMapper::toDto).toList();
        log.debug("Returning {} files (total: {})", items.size(), result.getTotalElements());

        return FileListResponse.builder().items(items).total(result.getTotalElements()).build();
    }
}
