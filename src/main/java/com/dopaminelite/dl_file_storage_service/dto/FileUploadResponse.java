package com.dopaminelite.dl_file_storage_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileUploadResponse {
    private StoredFileDto file;
    private String signedUrl;
    private OffsetDateTime expiresAt;
}

