package com.dopaminelite.dl_file_storage_service.entity;

import com.dopaminelite.dl_file_storage_service.constant.FileContextType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "dopaminelite_stored_files",
        indexes = {
                @Index(name = "idx_context_type_ref", columnList = "context_type, context_ref_id"),
                @Index(name = "idx_created_by", columnList = "created_by_user_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, unique = true)
    private String storedFileName;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    @Column(name = "sha256")
    private String sha256;

    @Column(name = "bucket", nullable = false)
    private String bucket;

    @Column(name = "storage_path", nullable = false, unique = true)
    private String storagePath;

    @Enumerated(EnumType.STRING)
    @Column(name = "context_type", nullable = false)
    private FileContextType contextType;

    @Column(name = "context_ref_id")
    private String contextRefId;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;
}
