package com.kaua.file.processor.infrastructure.repositories;

import com.kaua.file.processor.AbstractRepositoryTest;
import com.kaua.file.processor.domain.exceptions.ValidationException;
import com.kaua.file.processor.domain.importjob.ImportJob;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.jdbc.Sql;

class ImportJobRepositoryTest extends AbstractRepositoryTest {

    @Test
    void testAssertDependencies() {
        Assertions.assertNotNull(importJobRepository());
    }

    @Test
    void givenAValidNewImportJob_whenCallsSave_shouldPersistIt() {
        Assertions.assertEquals(0, countImportJobs());

        final var aFileRef = "file-ref";
        final var aFileHash = "file-hash";

        final var aImportJob = ImportJob.newImportJob(
                aFileRef,
                aFileHash
        );

        final var aActualImportJob = this.importJobRepository().save(aImportJob);

        Assertions.assertEquals(1, countImportJobs());

        Assertions.assertEquals(aImportJob.getId().value(), aActualImportJob.getId().value());
        Assertions.assertEquals(aImportJob.getFileRef(), aActualImportJob.getFileRef());
        Assertions.assertEquals(aImportJob.getFileHash(), aActualImportJob.getFileHash());
        Assertions.assertEquals(aImportJob.getStatus(), aActualImportJob.getStatus());
        Assertions.assertEquals(aImportJob.getCreatedAt(), aActualImportJob.getCreatedAt());
        Assertions.assertEquals(aImportJob.getUpdatedAt(), aActualImportJob.getUpdatedAt());
        Assertions.assertTrue(aActualImportJob.getDeletedAt().isEmpty());
    }

    @Test
    void givenAValidFileHash_whenCallsImportJobOfFileHash_shouldReturnIt() {
        Assertions.assertEquals(0, countImportJobs());

        final var aFileRef = "file-ref";
        final var aFileHash = "file-hash";

        final var aImportJob = ImportJob.newImportJob(
                aFileRef,
                aFileHash
        );

        this.importJobRepository().save(aImportJob);

        Assertions.assertEquals(1, countImportJobs());

        final var aActualImportJob = this.importJobRepository().importJobOfFileHash(aFileHash).get();

        Assertions.assertEquals(aImportJob.getId().value(), aActualImportJob.getId().value());
        Assertions.assertEquals(aImportJob.getFileRef(), aActualImportJob.getFileRef());
        Assertions.assertEquals(aImportJob.getFileHash(), aActualImportJob.getFileHash());
        Assertions.assertEquals(aImportJob.getStatus(), aActualImportJob.getStatus());
        Assertions.assertEquals(aImportJob.getCreatedAt(), aActualImportJob.getCreatedAt());
        Assertions.assertEquals(aImportJob.getUpdatedAt(), aActualImportJob.getUpdatedAt());
        Assertions.assertTrue(aActualImportJob.getDeletedAt().isEmpty());
    }

    @Test
    @Sql(statements = {
            "INSERT INTO import_jobs (id, file_ref, file_hash, status, created_at, updated_at, deleted_at, version) " +
                    "VALUES ('01FZ8Z5A6X6RXZ7GQ0MGY6N5K4', 'file-ref', 'file-hash', 'INVALID', NOW(), NOW(), NULL, 1)"
    })
    void givenAnInvalidImportJobStatusInDB_whenCallsImportJobOfFileHash_shouldThrowsValidationException() {
        Assertions.assertEquals(1, countImportJobs());

        final var aFileHash = "file-hash";

        final var aExpectedErrorMessage = "should not be null";
        final var aExpectedErrorProperty = "status";

        final var aActualException = Assertions.assertThrows(
                ValidationException.class,
                () -> this.importJobRepository().importJobOfFileHash(aFileHash)
        );

        Assertions.assertEquals(aExpectedErrorMessage, aActualException.getErrors().getFirst().message());
        Assertions.assertEquals(aExpectedErrorProperty, aActualException.getErrors().getFirst().property());
    }
}
