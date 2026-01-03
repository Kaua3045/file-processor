package com.kaua.file.processor.infrastructure.configurations;

import com.kaua.file.processor.application.repository.FileStorageRepository;
import com.kaua.file.processor.infrastructure.repositories.LocalFileStorageRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;

@Configuration(proxyBeanMethods = false)
public class FileStorageConfig {

    @Bean
    public FileStorageRepository localFileStorageRepository() {
        return new LocalFileStorageRepository(Path.of("./files"));
    }
}
