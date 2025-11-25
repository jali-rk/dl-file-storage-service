package com.dopaminelite.dl_file_storage_service.dto;

import lombok.*;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkFileSignedUrlRequestItem {
    private UUID fileId;
    private SignedUrlIntent intent; // VIEW / DOWNLOAD
}

