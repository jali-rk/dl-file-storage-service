package com.dopaminelite.dl_file_storage_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String localBasePath = "files-storage"; // storage.local.base-path
    private int signedUrlDefaultExpirationSeconds = 900; // storage.signed-url.default-expiration-seconds
}

