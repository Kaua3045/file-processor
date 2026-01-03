package com.kaua.file.processor.infrastructure.configurations.usecases;

import com.kaua.file.processor.application.importjob.create.CreateImportJobUseCase;
import com.kaua.file.processor.application.importjob.create.DefaultCreateImportJobUseCase;
import com.kaua.file.processor.application.repository.FileStorageRepository;
import com.kaua.file.processor.application.repository.ImportJobRepository;
import com.kaua.file.processor.application.wrapper.TracerWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ImportJobUseCases {

    @Bean
    public CreateImportJobUseCase createImportJobUseCase(
            final FileStorageRepository fileStorageRepository,
            final ImportJobRepository importJobRepository,
            final TracerWrapper tracerWrapper
    ) {
        return new DefaultCreateImportJobUseCase(
                fileStorageRepository,
                importJobRepository,
                tracerWrapper
        );
    }
}
