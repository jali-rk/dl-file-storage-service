package com.dopaminelite.dl_file_storage_service.repository;

import com.dopaminelite.dl_file_storage_service.constant.FileContextType;
import com.dopaminelite.dl_file_storage_service.entity.StoredFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
    Page<StoredFile> findByCreatedByUserIdAndContextTypeAndContextRefId(
            UUID createdByUserId,
            FileContextType contextType,
            String contextRefId,
            Pageable pageable);

    Page<StoredFile> findByCreatedByUserIdAndContextType(
            UUID createdByUserId,
            FileContextType contextType,
            Pageable pageable);

    Page<StoredFile> findByCreatedByUserId(UUID createdByUserId, Pageable pageable);

    Page<StoredFile> findByContextTypeAndContextRefId(FileContextType contextType, String contextRefId, Pageable pageable);

    Page<StoredFile> findByContextType(FileContextType contextType, Pageable pageable);
}

