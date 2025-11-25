package com.dopaminelite.dl_file_storage_service.controller;

import com.dopaminelite.dl_file_storage_service.dto.*;
import com.dopaminelite.dl_file_storage_service.entity.FileContextType;
import com.dopaminelite.dl_file_storage_service.service.FileStorageService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
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
public class FileController {

    private final FileStorageService fileStorageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestPart("file") MultipartFile file,
                                                         @RequestPart("createdByUserId") UUID createdByUserId,
                                                         @RequestPart("contextType") FileContextType contextType,
                                                         @RequestPart(value = "contextRefId", required = false) String contextRefId) {
        FileUploadResponse response = fileStorageService.uploadFile(file, createdByUserId, contextType, contextRefId, true);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<FileListResponse> listFiles(@RequestParam(value = "createdByUserId", required = false) UUID createdByUserId,
                                       @RequestParam(value = "contextType", required = false) FileContextType contextType,
                                       @RequestParam(value = "contextRefId", required = false) String contextRefId,
                                       @RequestParam(value = "limit", required = false, defaultValue = "20") int limit,
                                       @RequestParam(value = "offset", required = false, defaultValue = "0") int offset) {
        FileListResponse response = fileStorageService.listFiles(createdByUserId, contextType, contextRefId, limit, offset);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fileId}")
    public StoredFileDto getFile(@PathVariable UUID fileId) {
        return fileStorageService.getFile(fileId);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID fileId) {
        fileStorageService.softDeleteFile(fileId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{fileId}/signed-url")
    public FileSignedUrlResponse generateSignedUrl(@PathVariable UUID fileId,
                                                   @RequestParam(value = "intent", required = false, defaultValue = "VIEW") SignedUrlIntent intent,
                                                   @RequestParam(value = "expiresInSeconds", required = false) Integer expiresInSeconds) {
        return fileStorageService.generateSignedUrl(fileId, intent, expiresInSeconds);
    }

    @PostMapping("/signed-urls")
    public BulkFileSignedUrlResponse bulkSignedUrls(@RequestBody BulkFileSignedUrlRequest request,
                                                    @RequestParam(value = "expiresInSeconds", required = false) Integer expiresInSeconds) {
        return fileStorageService.bulkGenerateSignedUrls(request.getItems(), expiresInSeconds);
    }

    @Data
    private static class BulkFileSignedUrlRequest {
        private List<BulkFileSignedUrlRequestItem> items;
    }
}
