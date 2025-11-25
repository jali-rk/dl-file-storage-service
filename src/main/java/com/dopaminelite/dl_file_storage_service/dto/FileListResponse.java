package com.dopaminelite.dl_file_storage_service.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileListResponse {
    private List<StoredFileDto> items;
    private long total;
}

