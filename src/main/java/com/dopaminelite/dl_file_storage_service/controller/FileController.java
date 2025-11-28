package com.dopaminelite.dl_file_storage_service.controller;

import com.dopaminelite.dl_file_storage_service.constant.SignedUrlIntent;
import com.dopaminelite.dl_file_storage_service.constant.FileContextType;
import com.dopaminelite.dl_file_storage_service.dto.*;
import com.dopaminelite.dl_file_storage_service.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<FileUploadResponse>> uploadFiles(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam("createdByUserId") UUID createdByUserId,
            @RequestParam("contextType") FileContextType contextType,
            @RequestParam(value = "contextRefId", required = false) String contextRefId) {
        try {
            log.debug("Uploading {} file(s) for userId: {}, contextType: {}, contextRefId: {}",
                    files.size(), createdByUserId, contextType, contextRefId);

            List<FileUploadResponse> responses = files.stream()
                    .map(file -> fileStorageService.uploadFile(file, createdByUserId, contextType, contextRefId, true))
                    .toList();

            log.debug("Successfully uploaded {} file(s) for userId: {}", responses.size(), createdByUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (Exception e) {
            log.error("Error uploading files for userId: {}", createdByUserId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<FileListResponse> listFiles(
            @RequestParam(value = "createdByUserId", required = false) UUID createdByUserId,
            @RequestParam(value = "contextType", required = false) FileContextType contextType,
            @RequestParam(value = "contextRefId", required = false) String contextRefId,
            @RequestParam(value = "limit", required = false, defaultValue = "20") int limit,
            @RequestParam(value = "offset", required = false, defaultValue = "0") int offset) {
        log.debug("Listing files for userId: {}, contextType: {}, contextRefId: {}, limit: {}, offset: {}",
                createdByUserId, contextType, contextRefId, limit, offset);
        FileListResponse response = fileStorageService.listFiles(createdByUserId, contextType, contextRefId, limit, offset);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}")
    public StoredFileDto getFile(@PathVariable UUID fileId) {
        log.debug("Fetching file with id: {}", fileId);
        try {
            return fileStorageService.getFile(fileId);
        } catch (Exception e) {
            log.error("Error fetching file with id: {}", fileId, e);
            throw e;
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID fileId) {
        log.debug("Soft deleting file with id: {}", fileId);
        try {
            fileStorageService.softDeleteFile(fileId);
            log.debug("File with id: {} soft deleted successfully", fileId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting file with id: {}", fileId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{fileId}/signed-url")
    public FileSignedUrlResponse generateSignedUrl(@PathVariable UUID fileId,
                                                   @RequestParam(value = "intent", required = false, defaultValue = "VIEW") SignedUrlIntent intent,
                                                   @RequestParam(value = "expiresInSeconds", required = false) Integer expiresInSeconds) {
        log.debug("Generating signed URL for fileId: {}, intent: {}, expiresInSeconds: {}", fileId, intent, expiresInSeconds);
        try {
            return fileStorageService.generateSignedUrl(fileId, intent, expiresInSeconds);
        } catch (Exception e) {
            log.error("Error generating signed URL for fileId: {}", fileId, e);
            throw e;
        }
    }

    @PostMapping(value = "/signed-urls", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BulkFileSignedUrlResponse bulkSignedUrls(@RequestBody BulkSignedUrlsRequest request,
                                                    @RequestParam(value = "expiresInSeconds", required = false) Integer expiresInSeconds) {
        log.debug("Generating bulk signed URLs for {} files, expiresInSeconds: {}", request.getItems().size(), expiresInSeconds);
        try {
            return fileStorageService.bulkGenerateSignedUrls(request.getItems(), expiresInSeconds);
        } catch (Exception e) {
            log.error("Error generating bulk signed URLs", e);
            throw e;
        }
    }
}
