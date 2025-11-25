package com.dopaminelite.dl_file_storage_service.dto;

import lombok.*;
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

