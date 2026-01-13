package com.kaua.file.processor.application.importjob.progress;

import com.kaua.file.processor.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.file.processor.application.repository.ImportJobRepository;
import com.kaua.file.processor.application.wrapper.TracerWrapper;
import com.kaua.file.processor.domain.exceptions.NotFoundException;
import com.kaua.file.processor.domain.importjob.ImportJob;

import java.util.Objects;

public class DefaultGetImportJobProgressUseCase extends GetImportJobProgressUseCase {

    private final ImportJobRepository importJobRepository;
    private final TracerWrapper tracerWrapper;

    public DefaultGetImportJobProgressUseCase(
            final ImportJobRepository importJobRepository,
            final TracerWrapper tracerWrapper
    ) {
        this.importJobRepository = Objects.requireNonNull(importJobRepository);
        this.tracerWrapper = Objects.requireNonNull(tracerWrapper);
    }

    @Override
    public GetImportJobProgressOutput execute(final GetImportJobProgressCommand input) {
        return this.tracerWrapper.traceWithReturn("get-import-job-progress", (ctx) -> {
            if (input == null) {
                throw new UseCaseInputCannotBeNullException(GetImportJobProgressUseCase.class);
            }

            final var aImportJob = ctx.runInSpan("find-import-job", () ->
                    this.importJobRepository.importJobOfId(input.importJobId())
                            .orElseThrow(NotFoundException.with(ImportJob.class, input.importJobId()))
            );

            return GetImportJobProgressOutput.from(aImportJob);
        });
    }
}
