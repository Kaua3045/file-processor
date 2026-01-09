package com.kaua.file.processor.application.importjob.process;

import com.kaua.file.processor.application.UseCaseTest;
import com.kaua.file.processor.application.exceptions.UseCaseInputCannotBeNullException;
import com.kaua.file.processor.application.repository.FileStorageRepository;
import com.kaua.file.processor.application.repository.ImportJobRepository;
import com.kaua.file.processor.domain.exceptions.NotFoundException;
import com.kaua.file.processor.domain.importjob.ImportJob;
import com.kaua.file.processor.domain.importjob.ImportJobStatus;
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
import static org.mockito.ArgumentMatchers.argThat;

class ProcessImportJobUseCaseTest extends UseCaseTest {

    @Mock
    private ImportJobRepository importJobRepository;

    @Mock
    private FileStorageRepository fileStorageRepository;

    @InjectMocks
    private DefaultProcessImportJobUseCase useCase;

    @Test
    void givenAValidCommand_whenCallsProcessImportJobUseCase_executeShouldProcessTheImportJob() {
        final var aImportJob = ImportJob.newImportJob(
                "file-ref-123",
                "sha256-hash-abc"
        );
        final var aImportJobId = aImportJob.getId().value().toString();

        final var aCommand = ProcessImportJobCommand.with(aImportJobId);

        Mockito.when(importJobRepository.importJobOfId(aImportJobId))
                .thenReturn(Optional.of(aImportJob));
        Mockito.when(importJobRepository.save(any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(fileStorageRepository.load(aImportJob.getFileRef()))
                .thenReturn(mockInputStream());

        Assertions.assertDoesNotThrow(() -> this.useCase.execute(aCommand));

        Mockito.verify(importJobRepository, Mockito.times(1))
                .importJobOfId(aImportJobId);
        Mockito.verify(importJobRepository, Mockito.times(3))
                .save(argThat(aCmd -> {
                    if (aCmd == null) return false;
                    return switch (aCmd.getStatus()) {
                        case PROCESSING, COMPLETED -> true;
                        default -> false;
                    };
                }));
        Mockito.verify(fileStorageRepository, Mockito.times(1))
                .load(aImportJob.getFileRef());
    }

    @Test
    void givenANullCommand_whenCallsProcessImportJobUseCase_executeShouldThrowException() {
        final var expectedErrorMessage = "Input to ProcessImportJobUseCase cannot be null";

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
    void givenAnInvalidImportJobId_whenCallsProcessImportJobUseCase_executeShouldThrowException() {
        final var aCommand = ProcessImportJobCommand.with("invalid-id");

        final var expectedErrorMessage = "ImportJob with id invalid-id was not found";

        final var actualOutput = Assertions.assertThrows(
                NotFoundException.class,
                () -> this.useCase.execute(aCommand)
        );

        Assertions.assertNotNull(actualOutput);
        Assertions.assertEquals(
                expectedErrorMessage,
                actualOutput.getMessage()
        );

        Mockito.verify(importJobRepository, Mockito.times(1))
                .importJobOfId("invalid-id");
        Mockito.verify(importJobRepository, Mockito.never())
                .save(any());
        Mockito.verify(fileStorageRepository, Mockito.never())
                .load(any());
    }

    @Test
    void givenAnExceptionWhenLoadingFile_whenCallsProcessImportJobUseCase_executeShouldFailTheImportJob() {
        final var aImportJob = ImportJob.newImportJob(
                "file-ref-123",
                "sha256-hash-abc"
        );
        final var aImportJobId = aImportJob.getId().value().toString();

        final var aCommand = ProcessImportJobCommand.with(aImportJobId);

        Mockito.when(importJobRepository.importJobOfId(aImportJobId))
                .thenReturn(Optional.of(aImportJob));
        Mockito.when(importJobRepository.save(any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(fileStorageRepository.load(aImportJob.getFileRef()))
                .thenThrow(new RuntimeException("IO Error"));

        Assertions.assertDoesNotThrow(() -> this.useCase.execute(aCommand));

        Mockito.verify(importJobRepository, Mockito.times(1))
                .importJobOfId(aImportJobId);
        Mockito.verify(importJobRepository, Mockito.times(2))
                .save(argThat(aCmd -> {
                    if (aCmd == null) return false;
                    return switch (aCmd.getStatus()) {
                        case PROCESSING, FAILED -> true;
                        default -> false;
                    };
                }));
        Mockito.verify(fileStorageRepository, Mockito.times(1))
                .load(aImportJob.getFileRef());
    }

    @Test
    void givenAnFileWithMoreChunks_whenCallsProcessImportJobUseCase_executeShouldProcessAllChunks() {
        final var aImportJob = ImportJob.newImportJob(
                "file-ref-123",
                "sha256-hash-abc"
        );
        final var aImportJobId = aImportJob.getId().value().toString();

        final var aCommand = ProcessImportJobCommand.with(aImportJobId);

        Mockito.when(importJobRepository.importJobOfId(aImportJobId))
                .thenReturn(Optional.of(aImportJob));
        Mockito.when(importJobRepository.save(any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(fileStorageRepository.load(aImportJob.getFileRef()))
                .thenReturn(mockLargeInputStream());

        Assertions.assertDoesNotThrow(() -> this.useCase.execute(aCommand));

        Mockito.verify(importJobRepository, Mockito.times(1))
                .importJobOfId(aImportJobId);
        Mockito.verify(importJobRepository, Mockito.times(5))
                .save(argThat(aCmd -> {
                    if (aCmd == null) return false;
                    return switch (aCmd.getStatus()) {
                        case PROCESSING, COMPLETED -> true;
                        default -> false;
                    };
                }));
        Mockito.verify(fileStorageRepository, Mockito.times(1))
                .load(aImportJob.getFileRef());
    }

    @Test
    void givenAFileWithExactChunkSize_whenCallsProcessImportJobUseCase_shouldNotProcessLastChunk() {
        final var aImportJob = ImportJob.newImportJob(
                "file-ref-123",
                "sha256-hash-abc"
        );
        final var aImportJobId = aImportJob.getId().value().toString();

        final var aCommand = ProcessImportJobCommand.with(aImportJobId);

        Mockito.when(importJobRepository.importJobOfId(aImportJobId))
                .thenReturn(Optional.of(aImportJob));
        Mockito.when(importJobRepository.save(any()))
                .thenAnswer(returnsFirstArg());
        Mockito.when(fileStorageRepository.load(aImportJob.getFileRef()))
                .thenReturn(mockExactChunkInputStream());

        Assertions.assertDoesNotThrow(() -> this.useCase.execute(aCommand));

        Mockito.verify(importJobRepository, Mockito.times(1))
                .importJobOfId(aImportJobId);

        Mockito.verify(importJobRepository, Mockito.times(4))
                .save(argThat(job ->
                        job.getStatus() == ImportJobStatus.PROCESSING ||
                                job.getStatus() == ImportJobStatus.COMPLETED
                ));
    }

    private InputStream mockInputStream() {
        String content = "id,name\n1,Kaua\n2,Ana";
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    private InputStream mockLargeInputStream() {
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 1; i <= 2500; i++) {
            contentBuilder.append(i).append(",Name").append(i).append("\n");
        }
        return new ByteArrayInputStream(contentBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }

    private InputStream mockExactChunkInputStream() {
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 1; i <= 2000; i++) {
            contentBuilder.append(i).append(",Name").append(i).append("\n");
        }
        return new ByteArrayInputStream(
                contentBuilder.toString().getBytes(StandardCharsets.UTF_8)
        );
    }
}
