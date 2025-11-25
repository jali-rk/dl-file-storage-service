package com.dopaminelite.dl_file_storage_service.service;

import com.dopaminelite.dl_file_storage_service.config.StorageProperties;
import com.dopaminelite.dl_file_storage_service.dto.*;
import com.dopaminelite.dl_file_storage_service.entity.FileContextType;
import com.dopaminelite.dl_file_storage_service.entity.StoredFile;
import com.dopaminelite.dl_file_storage_service.exception.BadRequestException;
import com.dopaminelite.dl_file_storage_service.exception.NotFoundException;
import com.dopaminelite.dl_file_storage_service.mapper.FileMapper;
import com.dopaminelite.dl_file_storage_service.repository.StoredFileRepository;
import com.dopaminelite.dl_file_storage_service.storage.StorageProvider;
import lombok.RequiredArgsConstructor;
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
public class FileStorageServiceImpl implements FileStorageService {

    private final StoredFileRepository repository;
    private final StorageProvider storageProvider;
    private final StorageProperties storageProperties;

    @Override
    public FileUploadResponse uploadFile(MultipartFile file, UUID createdByUserId, FileContextType contextType, String contextRefId, boolean generateSignedUrl) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }
        String originalName = file.getOriginalFilename();
        String mimeType = file.getContentType();
        long sizeBytes = file.getSize();
        String storedFileName = UUID.randomUUID() + "_" + originalName;
        String bucket = contextType.name().toLowerCase();
        String bucketPath = bucket; // simplistic bucket mapping
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException e) {
            throw new BadRequestException("Unable to read file bytes: " + e.getMessage());
        }
        String storagePath;
        try {
            storagePath = storageProvider.store(content, storedFileName, bucketPath);
        } catch (IOException e) {
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
                .sha256(null) // TODO compute checksum if needed
                .isDeleted(false)
                .build();
        entity = repository.save(entity);
        StoredFileDto dto = FileMapper.toDto(entity);
        String signedUrl = null;
        OffsetDateTime expiresAt = null;
        if (generateSignedUrl) {
            int exp = storageProperties.getSignedUrlDefaultExpirationSeconds();
            signedUrl = storageProvider.generateSignedUrl(entity.getStoragePath(), SignedUrlIntent.VIEW, exp);
            expiresAt = OffsetDateTime.now().plusSeconds(exp);
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
        StoredFile entity = repository.findById(fileId).orElseThrow(() -> new NotFoundException("File not found: " + fileId));
        return FileMapper.toDto(entity);
    }

    @Override
    public void softDeleteFile(UUID fileId) {
        StoredFile entity = repository.findById(fileId).orElseThrow(() -> new NotFoundException("File not found: " + fileId));
        entity.setDeleted(true); // use Lombok-generated setter for boolean field 'isDeleted'
        repository.save(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public FileSignedUrlResponse generateSignedUrl(UUID fileId, SignedUrlIntent intent, Integer expiresInSeconds) {
        StoredFile entity = repository.findById(fileId).orElseThrow(() -> new NotFoundException("File not found: " + fileId));
        int exp = expiresInSeconds != null ? expiresInSeconds : storageProperties.getSignedUrlDefaultExpirationSeconds();
        String url = storageProvider.generateSignedUrl(entity.getStoragePath(), intent, exp);
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
            throw new BadRequestException("Items list must not be empty");
        }
        int exp = expiresInSeconds != null ? expiresInSeconds : storageProperties.getSignedUrlDefaultExpirationSeconds();
        List<BulkFileSignedUrlResponseItem> responses = new ArrayList<>();
        for (BulkFileSignedUrlRequestItem item : items) {
            StoredFile entity = repository.findById(item.getFileId()).orElseThrow(() -> new NotFoundException("File not found: " + item.getFileId()));
            String url = storageProvider.generateSignedUrl(entity.getStoragePath(), item.getIntent() == null ? SignedUrlIntent.VIEW : item.getIntent(), exp);
            responses.add(BulkFileSignedUrlResponseItem.builder()
                    .fileId(entity.getId())
                    .url(url)
                    .expiresAt(OffsetDateTime.now().plusSeconds(exp))
                    .build());
        }
        return BulkFileSignedUrlResponse.builder().items(responses).build();
    }

    @Override
    @Transactional(readOnly = true)
    public FileListResponse listFiles(UUID createdByUserId, FileContextType contextType, String contextRefId, int limit, int offset) {
        if (limit <= 0) throw new BadRequestException("limit must be > 0");
        if (limit > 100) throw new BadRequestException("limit must be <= 100");
        if (offset < 0) throw new BadRequestException("offset must be >= 0");
        int page = offset / limit;
        PageRequest pr = PageRequest.of(page, limit);
        Page<StoredFile> result;
        if (createdByUserId != null && contextType != null && contextRefId != null) {
            result = repository.findByCreatedByUserIdAndContextTypeAndContextRefId(createdByUserId, contextType, contextRefId, pr);
        } else if (createdByUserId != null && contextType != null) {
            result = repository.findByCreatedByUserIdAndContextType(createdByUserId, contextType, pr);
        } else if (createdByUserId != null) {
            result = repository.findByCreatedByUserId(createdByUserId, pr);
        } else if (contextType != null && contextRefId != null) {
            result = repository.findByContextTypeAndContextRefId(contextType, contextRefId, pr);
        } else if (contextType != null) {
            result = repository.findByContextType(contextType, pr);
        } else {
            result = repository.findAll(pr);
        }
        List<StoredFileDto> items = result.getContent().stream().map(FileMapper::toDto).toList();
        return FileListResponse.builder().items(items).total(result.getTotalElements()).build();
    }
}
