package com.kaua.file.processor.application.importjob.create;

import com.kaua.file.processor.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.file.processor.application.repository.FileStorageRepository;
import com.kaua.file.processor.application.repository.ImportJobRepository;
import com.kaua.file.processor.application.wrapper.TracerWrapper;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import com.kaua.file.processor.domain.exceptions.DomainException;
import com.kaua.file.processor.domain.importjob.ImportJob;

import java.util.Objects;
import java.util.Set;

public class DefaultCreateImportJobUseCase extends CreateImportJobUseCase {

    private final FileStorageRepository fileStorage;
    private final ImportJobRepository importJobRepository;
    private final TracerWrapper tracerWrapper;

    public DefaultCreateImportJobUseCase(
            final FileStorageRepository fileStorage,
            final ImportJobRepository importJobRepository,
            final TracerWrapper tracerWrapper
    ) {
        this.fileStorage = Objects.requireNonNull(fileStorage);
        this.importJobRepository = Objects.requireNonNull(importJobRepository);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public CreateImportJobOutput execute(final CreateImportJobCommand input) {
        return this.tracerWrapper.traceWithReturn(
                "CreateImportJobUseCase.execute",
                (ctx) -> {
                    if (input == null) {
                        throw new UseCaseInputCannotBeNullException(CreateImportJobUseCase.class);
                    }

                    if (!isAllowedType(input.fileName())) {
                        throw DomainException.with("File type not allowed: %s".formatted(input.fileName()));
                    }

                    final var aStoredFile = ctx.runInSpan("storeFile", () -> fileStorage.store(
                            input.fileName(),
                            input.fileContent()
                    ));

                    final var aImportJobOfFileHash = ctx.runInSpan("checkExistingImportJob", () ->
                            importJobRepository.importJobOfFileHash(aStoredFile.sha256())
                    );

                    if (aImportJobOfFileHash.isPresent()) {
                        ctx.runInSpan("deleteStoredFile", () -> fileStorage.delete(aStoredFile.fileRef()));

                        return new CreateImportJobOutput(
                                aImportJobOfFileHash.get().getId().value().toString(),
                                input.fileName(),
                                aImportJobOfFileHash.get().getStatus().name()
                        );
                    }

                    final var anImportJob = ctx.runInSpan("createImportJob", () -> ImportJob.newImportJob(
                            aStoredFile.fileRef(),
                            aStoredFile.sha256()
                    ));
                    anImportJob.registerEvent(new ImportJobCreatedEvent(
                            anImportJob.getId().value().toString(),
                            anImportJob.getVersion()
                    ));

                    ctx.runInSpan("saveImportJob", () -> importJobRepository.save(anImportJob));

                    return new CreateImportJobOutput(
                            anImportJob.getId().value().toString(),
                            input.fileName(),
                            anImportJob.getStatus().name()
                    );
                }
        );
    }

    private boolean isAllowedType(final String fileName) {
        final var aExtension = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
        return Set.of("csv", "ods", "xlsx").contains(aExtension);
    }
}
