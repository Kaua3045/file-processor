package com.kaua.file.processor.infrastructure.rest.controllers;

import com.kaua.file.processor.application.importjob.create.CreateImportJobCommand;
import com.kaua.file.processor.application.importjob.create.CreateImportJobUseCase;
import com.kaua.file.processor.application.importjob.progress.GetImportJobProgressCommand;
import com.kaua.file.processor.application.importjob.progress.GetImportJobProgressUseCase;
import com.kaua.file.processor.domain.exceptions.InternalErrorException;
import com.kaua.file.processor.infrastructure.importjob.res.CreateImportJobResponse;
import com.kaua.file.processor.infrastructure.importjob.res.GetImportJobProgressResponse;
import com.kaua.file.processor.infrastructure.rest.ImportJobAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
public class ImportJobRestController implements ImportJobAPI {

    private static final Logger log = LoggerFactory.getLogger(ImportJobRestController.class);

    private final CreateImportJobUseCase createImportJobUseCase;
    private final GetImportJobProgressUseCase getImportJobProgressUseCase;

    public ImportJobRestController(
            final CreateImportJobUseCase createImportJobUseCase,
            final GetImportJobProgressUseCase getImportJobProgressUseCase
    ) {
        this.createImportJobUseCase = Objects.requireNonNull(createImportJobUseCase);
        this.getImportJobProgressUseCase = Objects.requireNonNull(getImportJobProgressUseCase);
    }

    @Override
    public ResponseEntity<CreateImportJobResponse> importFile(final MultipartFile file) {
        log.info("Received request to import file `{}`", file.getOriginalFilename());

        try {
            final var aCommand = CreateImportJobCommand.with(
                    file.getInputStream(),
                    file.getOriginalFilename()
            );

            final var aResponse = this.createImportJobUseCase.execute(aCommand);

            log.info("Import job created with ID `{}` for file `{}` with status `{}`",
                    aResponse.importJobId(),
                    aResponse.fileName(),
                    aResponse.status()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(CreateImportJobResponse.from(aResponse));
        } catch (IOException e) {
            throw InternalErrorException.with("Error reading uploaded file");
        }
    }

    @Override
    public ResponseEntity<GetImportJobProgressResponse> getImportJobStatus(final String importJobId) {
        log.info("Received request to get progress for import job ID `{}`", importJobId);
        final var aResponse = this.getImportJobProgressUseCase.execute(GetImportJobProgressCommand.with(importJobId));
        log.info("Progress for import job ID `{}`: {}%", importJobId, aResponse.status());
        return ResponseEntity.ok(GetImportJobProgressResponse.from(aResponse));
    }
}
