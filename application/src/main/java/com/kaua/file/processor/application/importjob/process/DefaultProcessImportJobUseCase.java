package com.kaua.file.processor.application.importjob.process;

import com.kaua.file.processor.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.file.processor.application.repository.FileStorageRepository;
import com.kaua.file.processor.application.repository.ImportJobRepository;
import com.kaua.file.processor.domain.exceptions.NotFoundException;
import com.kaua.file.processor.domain.importjob.ImportJob;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DefaultProcessImportJobUseCase extends ProcessImportJobUseCase {

    private static final int CHUNK_SIZE = 1_000;

    private final ImportJobRepository importJobRepository;
    private final FileStorageRepository fileStorageRepository;

    public DefaultProcessImportJobUseCase(
            final ImportJobRepository importJobRepository,
            final FileStorageRepository fileStorageRepository
    ) {
        this.importJobRepository = Objects.requireNonNull(importJobRepository);
        this.fileStorageRepository = Objects.requireNonNull(fileStorageRepository);
    }

    @Override
    public void execute(final ProcessImportJobCommand input) {
        if (input == null) {
            throw new UseCaseInputCannotBeNullException(ProcessImportJobUseCase.class);
        }

        final var aImportJob = this.importJobRepository.importJobOfId(input.importJobId())
                .orElseThrow(NotFoundException.with(ImportJob.class, input.importJobId()));
        aImportJob.start();
        this.importJobRepository.save(aImportJob);

        try (InputStream in = this.fileStorageRepository.load(aImportJob.getFileRef())) {
            final var aReader = new BufferedReader(new InputStreamReader(in));

            List<String> batch = new ArrayList<>(CHUNK_SIZE);
            String line;

            while ((line = aReader.readLine()) != null) {
                batch.add(line);

                if (batch.size() == CHUNK_SIZE) {
                    processBatch(batch, aImportJob);
                    batch.clear();
                }
            }

            // Last chunk
            if (!batch.isEmpty()) {
                processBatch(batch, aImportJob);
            }

            aImportJob.complete();
            this.importJobRepository.save(aImportJob);

        } catch (final Exception ex) {
            aImportJob.fail();
            this.importJobRepository.save(aImportJob);
        }
    }

    private void processBatch(final List<String> lines, final ImportJob aImportJob) {
        // Simulate processing each line
        aImportJob.incrementProcessed(lines.size());
        this.importJobRepository.save(aImportJob);
    }
}
