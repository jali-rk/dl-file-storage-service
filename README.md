# DopamineLite File Storage Service

## Module Breakdown

### Entity
- `StoredFile`: JPA entity representing stored file metadata.
- `FileContextType`: Enum classifying file usage context.

### Repository
- `StoredFileRepository`: CRUD + filtered queries on `StoredFile`.

### DTOs
- `StoredFileDto`: Metadata projection returned externally.
- `FileUploadResponse`: Wraps uploaded file metadata + optional signed URL.
- `FileSignedUrlResponse`: Represents single signed URL result.
- `BulkFileSignedUrlRequestItem`, `BulkFileSignedUrlResponseItem`, `BulkFileSignedUrlResponse`: Bulk signed URL request/response models.
- `SignedUrlIntent`: Enum controlling header disposition behavior (inline vs attachment conceptually).

### Mapper
- `FileMapper`: Converts `StoredFile` entity to `StoredFileDto`.

### Service
- `FileStorageService`: Interface for file operations (upload, get, delete, signed URLs).
- `FileStorageServiceImpl`: Implements business logic, persistence, and delegating to `StorageProvider`.

### Storage Provider
- `StorageProvider`: Abstraction for underlying storage (S3, GCS, local, etc.).
- `LocalStorageProvider`: Simple local-disk implementation (development only).

### Configuration
- `StorageProperties`: Binds `storage.*` configuration values.
- `StorageConfig`: Wires up `StorageProvider` bean.

### Controller
- `FileController`: REST API endpoints fulfilling OpenAPI specification.

### Exceptions & Handling
- `NotFoundException`, `BadRequestException`: Domain exceptions.
- `GlobalExceptionHandler`: Translates exceptions to consistent error payloads.

## PostgreSQL Configuration
Set in `application.properties`:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/dopamine_file_storage
spring.datasource.username=postgres
spring.datasource.password=admin
spring.jpa.hibernate.ddl-auto=update
```

## Build & Run
```cmd
gradlew.bat build
gradlew.bat bootRun
```

## Notes / Next Steps
- Implement real filtering/pagination in `FileController#listFiles`.
- Replace local storage with cloud provider (e.g., S3) and proper signed URL generation.
- Add checksum calculation (SHA-256) during upload.
- Add validation for allowed MIME types and max file size limits.
- Secure endpoints with service authentication (API key header) per OpenAPI.
package com.dopaminelite.dl_file_storage_service.dto;

import com.dopaminelite.dl_file_storage_service.entity.FileContextType;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredFileDto {
    private UUID id;
    private String originalFileName;
    private String storedFileName;
    private String mimeType;
    private long sizeBytes;
    private String sha256;
    private String bucket;
    private String storagePath;
    private FileContextType contextType;
    private String contextRefId;
    private UUID createdByUserId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private boolean isDeleted;
}

