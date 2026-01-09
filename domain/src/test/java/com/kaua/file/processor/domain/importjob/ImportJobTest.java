package com.kaua.file.processor.domain.importjob;

import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import com.kaua.file.processor.domain.exceptions.ValidationException;
import com.kaua.file.processor.domain.utils.ULID;
import com.kaua.file.processor.domain.validation.handler.NotificationHandler;
import org.junit.jupiter.api.Test;

import static com.kaua.file.processor.domain.utils.InstantUtils.now;
import static org.junit.jupiter.api.Assertions.*;

class ImportJobTest extends UnitTest {

    @Test
    void givenAValidParams_whenCallsNewImportJob_thenInstantiateACorrectObject() {
        final var expectedFileRef = "file-ref";
        final var expectedFileHash = "file-hash";
        final var expectedStatus = ImportJobStatus.CREATED;

        final var actualImportJob = ImportJob.newImportJob(
                expectedFileRef,
                expectedFileHash
        );

        assertEquals(expectedFileRef, actualImportJob.getFileRef());
        assertEquals(expectedFileHash, actualImportJob.getFileHash());
        assertEquals(expectedStatus, actualImportJob.getStatus());
        assertNotNull(actualImportJob.getId());
        assertNotNull(actualImportJob.getCreatedAt());
        assertNotNull(actualImportJob.getUpdatedAt());
        assertTrue(actualImportJob.getDeletedAt().isEmpty());
        assertEquals(0, actualImportJob.getProcessedRows());
        assertDoesNotThrow(() -> actualImportJob.validate(NotificationHandler.create()));
    }

    @Test
    void givenAnInvalidNullFileRef_whenCallsNewImportJob_thenShouldReceiveError() {
        final String expectedFileRef = null;
        final var expectedFileHash = "file-hash";

        final var actualException = assertThrows(
                ValidationException.class,
                () -> ImportJob.newImportJob(expectedFileRef, expectedFileHash)
        );

        assertEquals("should not be empty", actualException.getErrors().getFirst().message());
        assertEquals("fileRef", actualException.getErrors().getFirst().property());
    }

    @Test
    void givenAnInvalidEmptyFileRef_whenCallsNewImportJob_thenShouldReceiveError() {
        final var expectedFileRef = " ";
        final var expectedFileHash = "file-hash";

        final var actualException = assertThrows(
                ValidationException.class,
                () -> ImportJob.newImportJob(expectedFileRef, expectedFileHash)
        );

        assertEquals("should not be empty", actualException.getErrors().getFirst().message());
        assertEquals("fileRef", actualException.getErrors().getFirst().property());
    }

    @Test
    void givenAValidImportJobStatusName_whenCallsFromStatus_thenReturnIt() {
        final var expectedStatusName = "PROCESSING";

        final var actualImportJobStatus = ImportJobStatus.from(expectedStatusName);

        assertTrue(actualImportJobStatus.isPresent());
        assertEquals(ImportJobStatus.PROCESSING, actualImportJobStatus.get());
    }

    @Test
    void givenAnInvalidImportJobStatusName_whenCallsFromStatus_thenReturnEmpty() {
        final var expectedStatusName = "INVALID_STATUS";

        final var actualImportJobStatus = ImportJobStatus.from(expectedStatusName);

        assertTrue(actualImportJobStatus.isEmpty());
    }

    @Test
    void testCallImportJobToString() {
        final var importJob = ImportJob.newImportJob("file-ref", "file-hash");

        final var toStringResult = importJob.toString();

        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("fileRef='file-ref'"));
        assertTrue(toStringResult.contains("status=CREATED"));
    }

    @Test
    void givenAValidImportJobCreatedEvent_whenCallsRegisterEvent_thenShouldRegisterIt() {
        final var importJob = ImportJob.newImportJob("file-ref", "file-hash");
        final var event = new ImportJobCreatedEvent(
                importJob.getId().value().toString(),
                importJob.getVersion()
        );

        importJob.registerEvent(event);

        assertEquals(1, importJob.getDomainEvents().size());
        assertEquals(event, importJob.getDomainEvents().getFirst());
    }

    @Test
    void givenAValidParams_whenCallsWith_thenInstantiateACorrectObject() {
        final var expectedId = new ImportJobId(ULID.random());
        final var expectedVersion = 2L;
        final var expectedFileRef = "file-ref";
        final var expectedFileHash = "file-hash";
        final var expectedStatus = ImportJobStatus.PROCESSING;
        final var expectedCreatedAt = now();
        final var expectedUpdatedAt = now();
        final var expectedDeletedAt = now();
        final var expectedProcessedRows = 150L;

        final var actualImportJob = ImportJob.with(
                expectedId,
                expectedVersion,
                expectedFileRef,
                expectedFileHash,
                expectedStatus,
                expectedCreatedAt,
                expectedUpdatedAt,
                expectedDeletedAt,
                expectedProcessedRows
        );

        assertEquals(expectedId, actualImportJob.getId());
        assertEquals(expectedVersion, actualImportJob.getVersion());
        assertEquals(expectedFileRef, actualImportJob.getFileRef());
        assertEquals(expectedFileHash, actualImportJob.getFileHash());
        assertEquals(expectedStatus, actualImportJob.getStatus());
        assertEquals(expectedCreatedAt, actualImportJob.getCreatedAt());
        assertEquals(expectedUpdatedAt, actualImportJob.getUpdatedAt());
        assertTrue(actualImportJob.getDeletedAt().isPresent());
        assertEquals(expectedDeletedAt, actualImportJob.getDeletedAt().get());
        assertEquals(expectedProcessedRows, actualImportJob.getProcessedRows());
    }

    @Test
    void givenAValidCount_whenCallsIncrementProcessed_thenShouldIncrementProcessedRows() {
        final var importJob = ImportJob.newImportJob("file-ref", "file-hash");
        final var expectedCount = 5L;
        final var expectedProcessedRows = importJob.getProcessedRows() + expectedCount;

        importJob.incrementProcessed(expectedCount);

        assertEquals(expectedProcessedRows, importJob.getProcessedRows());
    }

    @Test
    void givenAValidImportJob_whenCallsStart_thenShouldUpdateStatusAndUpdatedAt() {
        final var importJob = ImportJob.newImportJob("file-ref", "file-hash");

        importJob.start();

        assertEquals(ImportJobStatus.PROCESSING, importJob.getStatus());
        assertNotNull(importJob.getUpdatedAt());
    }

    @Test
    void givenAValidImportJob_whenCallsComplete_thenShouldUpdateStatusAndUpdatedAt() {
        final var importJob = ImportJob.newImportJob("file-ref", "file-hash");

        importJob.complete();

        assertEquals(ImportJobStatus.COMPLETED, importJob.getStatus());
        assertNotNull(importJob.getUpdatedAt());
    }

    @Test
    void givenAValidImportJob_whenCallsFail_thenShouldUpdateStatusAndUpdatedAt() {
        final var importJob = ImportJob.newImportJob("file-ref", "file-hash");

        importJob.fail();

        assertEquals(ImportJobStatus.FAILED, importJob.getStatus());
        assertNotNull(importJob.getUpdatedAt());
    }
}
