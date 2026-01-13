package com.kaua.file.processor.infrastructure.configurations.usecases;

import com.kaua.file.processor.application.importjob.create.CreateImportJobUseCase;
import com.kaua.file.processor.application.importjob.create.DefaultCreateImportJobUseCase;
import com.kaua.file.processor.application.importjob.process.DefaultProcessImportJobUseCase;
import com.kaua.file.processor.application.importjob.process.ProcessImportJobUseCase;
import com.kaua.file.processor.application.importjob.progress.DefaultGetImportJobProgressUseCase;
import com.kaua.file.processor.application.importjob.progress.GetImportJobProgressUseCase;
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

    @Bean
    public ProcessImportJobUseCase processImportJobUseCase(
            final ImportJobRepository importJobRepository,
            final FileStorageRepository fileStorageRepository
    ) {
        return new DefaultProcessImportJobUseCase(
                importJobRepository,
                fileStorageRepository
        );
    }

    @Bean
    public GetImportJobProgressUseCase getImportJobProgressUseCase(
            final ImportJobRepository importJobRepository,
            final TracerWrapper tracerWrapper
    ) {
        return new DefaultGetImportJobProgressUseCase(
                importJobRepository,
                tracerWrapper
        );
    }
}
