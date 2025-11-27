package com.dopaminelite.dl_file_storage_service.dto;

import com.dopaminelite.dl_file_storage_service.constant.SignedUrlIntent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkFileSignedUrlRequestItem {
    private UUID fileId;
    private SignedUrlIntent intent; // VIEW / DOWNLOAD
}

