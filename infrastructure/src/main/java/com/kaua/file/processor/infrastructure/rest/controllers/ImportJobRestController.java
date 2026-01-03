package com.kaua.file.processor.infrastructure.rest.controllers;

import com.kaua.file.processor.application.importjob.create.CreateImportJobCommand;
import com.kaua.file.processor.application.importjob.create.CreateImportJobUseCase;
import com.kaua.file.processor.domain.exceptions.InternalErrorException;
import com.kaua.file.processor.infrastructure.importjob.res.CreateImportJobResponse;
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

    public ImportJobRestController(
            final CreateImportJobUseCase createImportJobUseCase
    ) {
        this.createImportJobUseCase = Objects.requireNonNull(createImportJobUseCase);
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
}
