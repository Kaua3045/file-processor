package com.kaua.file.processor.infrastructure.configurations;

import com.kaua.file.processor.application.repository.FileStorageRepository;
import com.kaua.file.processor.infrastructure.repositories.LocalFileStorageRepository;
import com.kaua.file.processor.infrastructure.repositories.S3FileStorageRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.s3.S3Client;

import java.nio.file.Path;

@Configuration(proxyBeanMethods = false)
public class FileStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "application.filestorage", havingValue = "local")
    public FileStorageRepository localFileStorageRepository() {
        return new LocalFileStorageRepository(Path.of("./files"));
    }

    @Bean
    @ConditionalOnProperty(name = "application.filestorage", havingValue = "s3")
    public FileStorageRepository s3FileStorageRepository(final S3Client s3Client, @Value("${aws.s3.bucket}") final String bucket) {
        return new S3FileStorageRepository(s3Client, bucket);
    }
}
