package com.kaua.file.processor.domain.importjob;

import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import com.kaua.file.processor.domain.exceptions.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportJobTest extends UnitTest {

    @Test
    void givenAValidParams_whenCallsNewImportJob_thenInstantiateACorrectObject() {
        final var expectedFileRef = "file-ref";
        final var expectedStatus = ImportJobStatus.CREATED;

        final var actualImportJob = ImportJob.newImportJob(
                expectedFileRef
        );

        assertEquals(expectedFileRef, actualImportJob.getFileRef());
        assertEquals(expectedStatus, actualImportJob.getStatus());
        assertNotNull(actualImportJob.getId());
        assertNotNull(actualImportJob.getCreatedAt());
        assertNotNull(actualImportJob.getUpdatedAt());
        assertTrue(actualImportJob.getDeletedAt().isEmpty());
    }

    @Test
    void givenAnInvalidNullFileRef_whenCallsNewImportJob_thenShouldReceiveError() {
        final String expectedFileRef = null;

        final var actualException = assertThrows(
                ValidationException.class,
                () -> ImportJob.newImportJob(expectedFileRef)
        );

        assertEquals("should not be empty", actualException.getErrors().getFirst().message());
        assertEquals("fileRef", actualException.getErrors().getFirst().property());
    }

    @Test
    void givenAnInvalidEmptyFileRef_whenCallsNewImportJob_thenShouldReceiveError() {
        final var expectedFileRef = " ";

        final var actualException = assertThrows(
                ValidationException.class,
                () -> ImportJob.newImportJob(expectedFileRef)
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
        final var importJob = ImportJob.newImportJob("file-ref");

        final var toStringResult = importJob.toString();

        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("fileRef='file-ref'"));
        assertTrue(toStringResult.contains("status=CREATED"));
    }

    @Test
    void givenAValidImportJobCreatedEvent_whenCallsRegisterEvent_thenShouldRegisterIt() {
        final var importJob = ImportJob.newImportJob("file-ref");
        final var event = new ImportJobCreatedEvent(
                importJob.getId().value().toString(),
                importJob.getVersion()
        );

        importJob.registerEvent(event);

        assertEquals(1, importJob.getDomainEvents().size());
        assertEquals(event, importJob.getDomainEvents().getFirst());
    }
}
