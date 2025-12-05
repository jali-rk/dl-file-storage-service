package com.dopaminelite.dl_file_storage_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    @NestedConfigurationProperty
    private SignedUrlProperties signedUrl = new SignedUrlProperties();

    // S3 configuration
    @NestedConfigurationProperty
    private S3Properties s3 = new S3Properties();

    @Getter
    @Setter
    public static class SignedUrlProperties {
        private int defaultExpirationSeconds = 900;
    }

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
