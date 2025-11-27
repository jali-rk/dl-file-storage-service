package com.dopaminelite.dl_file_storage_service.repository;

import com.dopaminelite.dl_file_storage_service.constant.FileContextType;
import com.dopaminelite.dl_file_storage_service.entity.StoredFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StoredFileRepository extends JpaRepository<StoredFile, UUID> {
    Page<StoredFile> findByCreatedByUserIdAndContextTypeAndContextRefIdAndIsDeletedFalse(
            UUID createdByUserId,
            FileContextType contextType,
            String contextRefId,
            Pageable pageable);

    Page<StoredFile> findByCreatedByUserIdAndContextTypeAndIsDeletedFalse(
            UUID createdByUserId,
            FileContextType contextType,
            Pageable pageable);

    Page<StoredFile> findByCreatedByUserIdAndIsDeletedFalse(UUID createdByUserId, Pageable pageable);

    Page<StoredFile> findByContextTypeAndContextRefIdAndIsDeletedFalse(
            FileContextType contextType,
            String contextRefId,
            Pageable pageable);

    Page<StoredFile> findByContextTypeAndIsDeletedFalse(FileContextType contextType, Pageable pageable);

    Page<StoredFile> findByIsDeletedFalse(Pageable pageable);

    Optional<StoredFile> findByIdAndIsDeletedFalse(UUID id);
}
