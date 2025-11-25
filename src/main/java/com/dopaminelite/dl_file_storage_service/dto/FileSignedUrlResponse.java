package com.dopaminelite.dl_file_storage_service.dto;

import lombok.*;
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

