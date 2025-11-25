package com.dopaminelite.dl_file_storage_service.config;

import com.dopaminelite.dl_file_storage_service.storage.LocalStorageProvider;
import com.dopaminelite.dl_file_storage_service.storage.StorageProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@RequiredArgsConstructor
public class StorageConfig {

    private final StorageProperties properties;

    @Bean
    public StorageProvider storageProvider() {
        return new LocalStorageProvider(properties.getLocalBasePath());
    }
}

