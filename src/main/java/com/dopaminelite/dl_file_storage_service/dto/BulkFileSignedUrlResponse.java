package com.dopaminelite.dl_file_storage_service.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkFileSignedUrlResponse {
    private List<BulkFileSignedUrlResponseItem> items;
}

