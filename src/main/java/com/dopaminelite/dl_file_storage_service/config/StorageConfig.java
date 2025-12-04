package com.dopaminelite.dl_file_storage_service.config;

import com.dopaminelite.dl_file_storage_service.storage.LocalStorageProvider;
import com.dopaminelite.dl_file_storage_service.storage.S3StorageProvider;
import com.dopaminelite.dl_file_storage_service.storage.StorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
@RequiredArgsConstructor
@Slf4j
public class StorageConfig {

    private final StorageProperties properties;

    @Bean
    public StorageProvider storageProvider() {
        String provider = properties.getProvider();
        log.info("Initializing storage provider: {}", provider);

        if ("s3".equalsIgnoreCase(provider)) {
            return createS3StorageProvider();
        } else {
            return new LocalStorageProvider(properties.getLocalBasePath());
        }
    }

    private StorageProvider createS3StorageProvider() {
        StorageProperties.S3Properties s3Props = properties.getS3();

        if (s3Props.getBucketName() == null || s3Props.getBucketName().isEmpty()) {
            throw new IllegalStateException("S3 bucket name is required when using S3 storage provider");
        }

        S3ClientBuilder s3ClientBuilder = S3Client.builder()
                .region(Region.of(s3Props.getRegion()));

        // Configure credentials if provided
        if (s3Props.getAccessKeyId() != null && s3Props.getSecretAccessKey() != null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    s3Props.getAccessKeyId(),
                    s3Props.getSecretAccessKey()
            );
            s3ClientBuilder.credentialsProvider(StaticCredentialsProvider.create(credentials));
            log.info("Using static AWS credentials for S3");
        } else {
            log.info("Using default AWS credentials chain for S3");
        }

        // Configure custom endpoint if provided (for S3-compatible services)
        if (s3Props.getEndpoint() != null && !s3Props.getEndpoint().isEmpty()) {
            s3ClientBuilder.endpointOverride(URI.create(s3Props.getEndpoint()));
            log.info("Using custom S3 endpoint: {}", s3Props.getEndpoint());
        }

        S3Client s3Client = s3ClientBuilder.build();

        // Create S3 Presigner with same configuration
        S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                .region(Region.of(s3Props.getRegion()));

        if (s3Props.getAccessKeyId() != null && s3Props.getSecretAccessKey() != null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(
                    s3Props.getAccessKeyId(),
                    s3Props.getSecretAccessKey()
            );
            presignerBuilder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        }

        if (s3Props.getEndpoint() != null && !s3Props.getEndpoint().isEmpty()) {
            presignerBuilder.endpointOverride(URI.create(s3Props.getEndpoint()));
        }

        S3Presigner s3Presigner = presignerBuilder.build();

        return new S3StorageProvider(s3Client, s3Presigner, s3Props.getBucketName());
    }
}
