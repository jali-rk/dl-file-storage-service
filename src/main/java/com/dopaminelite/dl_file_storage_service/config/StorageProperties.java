package com.dopaminelite.dl_file_storage_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String provider = "local"; // "local" or "s3"
    private String localBasePath = "files-storage"; // storage.local.base-path
    private int signedUrlDefaultExpirationSeconds = 900; // storage.signed-url.default-expiration-seconds

    // S3 configuration
    private S3Properties s3 = new S3Properties();

    @Getter
    @Setter
    public static class S3Properties {
        private String bucketName;
        private String region;
        private String accessKeyId;
        private String secretAccessKey;
        private String endpoint; // Optional: for S3-compatible services
    }
}
