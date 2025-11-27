package com.dopaminelite.dl_file_storage_service.service;

import com.dopaminelite.dl_file_storage_service.constant.FileContextType;
import com.dopaminelite.dl_file_storage_service.constant.SignedUrlIntent;
import com.dopaminelite.dl_file_storage_service.dto.BulkFileSignedUrlRequestItem;
import com.dopaminelite.dl_file_storage_service.dto.BulkFileSignedUrlResponse;
import com.dopaminelite.dl_file_storage_service.dto.FileListResponse;
import com.dopaminelite.dl_file_storage_service.dto.FileSignedUrlResponse;
import com.dopaminelite.dl_file_storage_service.dto.FileUploadResponse;
import com.dopaminelite.dl_file_storage_service.dto.StoredFileDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface FileStorageService {
    FileUploadResponse uploadFile(MultipartFile file,
                                  UUID createdByUserId,
                                  FileContextType contextType,
                                  String contextRefId,
                                  boolean generateSignedUrl);

    StoredFileDto getFile(UUID fileId);

    void softDeleteFile(UUID fileId);

    FileSignedUrlResponse generateSignedUrl(UUID fileId, SignedUrlIntent intent, Integer expiresInSeconds);

    BulkFileSignedUrlResponse bulkGenerateSignedUrls(List<BulkFileSignedUrlRequestItem> items, Integer expiresInSeconds);

    FileListResponse listFiles(UUID createdByUserId,
                               FileContextType contextType,
                               String contextRefId,
                               int limit,
                               int offset);
}
