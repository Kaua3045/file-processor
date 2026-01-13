package com.kaua.file.processor.application.importjob.progress;

import com.kaua.file.processor.application.UseCaseTest;
import com.kaua.file.processor.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.file.processor.application.repository.ImportJobRepository;
import com.kaua.file.processor.domain.importjob.ImportJob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

class GetImportJobProgressUseCaseTest extends UseCaseTest {

    @Mock
    private ImportJobRepository importJobRepository;

    @InjectMocks
    private DefaultGetImportJobProgressUseCase useCase;

    @Test
    void givenAValidCommand_whenCallsGetImportJobProgressUseCase_executeShouldReturnImportJobProgress() {
        final var existingImportJob = ImportJob.newImportJob("file-ref-123", "sha256-hash-abc");
        existingImportJob.start();

        final var aCommand = GetImportJobProgressCommand.with(existingImportJob.getId().value().toString());

        Mockito.when(importJobRepository.importJobOfId(any()))
                .thenReturn(Optional.of(existingImportJob));

        final var actualOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aCommand));

        Assertions.assertNotNull(actualOutput);
        Assertions.assertEquals(existingImportJob.getId().value().toString(), actualOutput.importJobId());
        Assertions.assertEquals(existingImportJob.getStatus().name(), actualOutput.status());
        Assertions.assertEquals(existingImportJob.getVersion(), actualOutput.version());

        Mockito.verify(importJobRepository, Mockito.times(1))
                .importJobOfId(existingImportJob.getId().value().toString());
    }

    @Test
    void givenANullCommand_whenCallsGetImportJobProgressUseCase_executeShouldThrowException() {
        final var expectedErrorMessage = "Input to GetImportJobProgressUseCase cannot be null";

        final var actualOutput = Assertions.assertThrows(
                UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null)
        );

        Assertions.assertNotNull(actualOutput);
        Assertions.assertEquals(
                expectedErrorMessage,
                actualOutput.getMessage()
        );
    }

    @Test
    void givenAnInvalidImportJobId_whenCallsGetImportJobProgressUseCase_executeShouldThrowException() {
        final var expectedErrorMessage = "ImportJob with id invalid-id-123 was not found";

        final var aCommand = GetImportJobProgressCommand.with("invalid-id-123");

        Mockito.when(importJobRepository.importJobOfId(any()))
                .thenReturn(Optional.empty());

        final var actualOutput = Assertions.assertThrows(
                Exception.class,
                () -> this.useCase.execute(aCommand)
        );

        Assertions.assertNotNull(actualOutput);
        Assertions.assertEquals(
                expectedErrorMessage,
                actualOutput.getMessage()
        );

        Mockito.verify(importJobRepository, Mockito.times(1))
                .importJobOfId("invalid-id-123");
    }
}
