package com.dopaminelite.dl_file_storage_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSignedUrlResponse {
    private UUID fileId;
    private String url;
    private OffsetDateTime expiresAt;
}

