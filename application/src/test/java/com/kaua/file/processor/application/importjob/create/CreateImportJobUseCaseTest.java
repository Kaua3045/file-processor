package com.kaua.file.processor.application.importjob.create;

import com.kaua.file.processor.application.UseCaseTest;
import com.kaua.file.processor.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.file.processor.application.repository.FileStorageRepository;
import com.kaua.file.processor.application.repository.ImportJobRepository;
import com.kaua.file.processor.domain.exceptions.DomainException;
import com.kaua.file.processor.domain.importjob.ImportJob;
import com.kaua.file.processor.domain.importjob.StoredFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;

class CreateImportJobUseCaseTest extends UseCaseTest {

    @Mock
    private FileStorageRepository fileStorageRepository;

    @Mock
    private ImportJobRepository importJobRepository;

    @InjectMocks
    private DefaultCreateImportJobUseCase useCase;

    @Test
    void givenAValidCommand_whenCallsCreateImportJobUseCase_executeShouldReturnImportJobId() {
        final var expectedFileName = "file.csv";
        final var expectedFileContent = mockInputStream();

        final var aCommand = CreateImportJobCommand.with(expectedFileContent, expectedFileName);

        Mockito.when(fileStorageRepository.store(expectedFileName, expectedFileContent))
                .thenReturn(mockStoredFile());
        Mockito.when(importJobRepository.importJobOfFileHash(any()))
                .thenReturn(Optional.empty());
        Mockito.when(importJobRepository.save(any()))
                .thenAnswer(returnsFirstArg());

        final var actualOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aCommand));

        Assertions.assertNotNull(actualOutput);
        Assertions.assertNotNull(actualOutput.importJobId());
        Assertions.assertEquals(expectedFileName, actualOutput.fileName());
        Assertions.assertEquals("CREATED", actualOutput.status());

        Mockito.verify(fileStorageRepository, Mockito.times(1))
                .store(expectedFileName, expectedFileContent);
        Mockito.verify(importJobRepository, Mockito.times(1))
                .importJobOfFileHash("sha256-hash-abc");
        Mockito.verify(importJobRepository, Mockito.times(1))
                .save(any());
    }

    @Test
    void givenAnExistingFileHash_whenCallsCreateImportJobUseCase_executeShouldReturnImportJobId() {
        final var expectedFileName = "file.csv";
        final var expectedFileContent = mockInputStream();

        final var aCommand = CreateImportJobCommand.with(expectedFileContent, expectedFileName);
        final var existingImportJob = ImportJob.newImportJob("file-ref-123", "sha256-hash-abc");

        Mockito.when(fileStorageRepository.store(expectedFileName, expectedFileContent))
                .thenReturn(mockStoredFile());
        Mockito.when(importJobRepository.importJobOfFileHash(any()))
                .thenReturn(Optional.of(existingImportJob));

        final var actualOutput = Assertions.assertDoesNotThrow(() -> this.useCase.execute(aCommand));

        Assertions.assertNotNull(actualOutput);
        Assertions.assertEquals(existingImportJob.getId().value().toString(), actualOutput.importJobId());
        Assertions.assertEquals(expectedFileName, actualOutput.fileName());
        Assertions.assertEquals(existingImportJob.getStatus().name(), actualOutput.status());

        Mockito.verify(fileStorageRepository, Mockito.times(1))
                .store(expectedFileName, expectedFileContent);
        Mockito.verify(importJobRepository, Mockito.times(1))
                .importJobOfFileHash("sha256-hash-abc");
        Mockito.verify(importJobRepository, Mockito.never())
                .save(any());
    }

    @Test
    void givenANullCommand_whenCallsCreateImportJobUseCase_executeShouldThrowException() {
        final var actualOutput = Assertions.assertThrows(
                UseCaseInputCannotBeNullException.class,
                () -> this.useCase.execute(null)
        );

        Assertions.assertNotNull(actualOutput);
        Assertions.assertEquals(
                "Input to CreateImportJobUseCase cannot be null",
                actualOutput.getMessage()
        );
    }

    @Test
    void givenAnInvalidFileType_whenCallsCreateImportJobUseCase_executeShouldThrowException() {
        final var expectedFileName = "file.txt";
        final var expectedFileContent = mockInputStream();

        final var aCommand = CreateImportJobCommand.with(expectedFileContent, expectedFileName);

        final var actualOutput = Assertions.assertThrows(
                DomainException.class,
                () -> this.useCase.execute(aCommand)
        );

        Assertions.assertNotNull(actualOutput);
        Assertions.assertEquals(
                "File type not allowed: file.txt",
                actualOutput.getMessage()
        );
    }

    private InputStream mockInputStream() {
        String content = "id,name\n1,Kaua\n2,Ana";
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private StoredFile mockStoredFile() {
        return new StoredFile(
                "file-ref-123",
                "sha256-hash-abc",
                1024L
        );
    }
}
