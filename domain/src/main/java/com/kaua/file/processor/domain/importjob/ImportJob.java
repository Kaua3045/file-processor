package com.kaua.file.processor.domain.importjob;

import com.kaua.file.processor.domain.AggregateRoot;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.domain.utils.InstantUtils;
import com.kaua.file.processor.domain.validation.ValidationHandler;

import java.time.Instant;
import java.util.Optional;

public class ImportJob extends AggregateRoot<ImportJobId> {

    private String fileRef;
    private String fileHash;
    private ImportJobStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;

    private ImportJob(
            final ImportJobId aImportJobId,
            final long aVersion,
            final String aFileRef,
            final String aFileHash,
            final ImportJobStatus aStatus,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aDeletedAt
    ) {
        super(aImportJobId, aVersion);
        setFileRef(aFileRef);
        setFileHash(aFileHash);
        setStatus(aStatus);
        setCreatedAt(aCreatedAt);
        setUpdatedAt(aUpdatedAt);
        setDeletedAt(aDeletedAt);
    }

    public static ImportJob newImportJob(final String aFileRef, final String aFileHash) {
        final var aNow = InstantUtils.now();

        return new ImportJob(
                new ImportJobId(IdentifierUtils.generateNewMonotonicULID()),
                0,
                aFileRef,
                aFileHash,
                ImportJobStatus.CREATED,
                aNow,
                aNow,
                null
        );
    }

    public String getFileRef() {
        return fileRef;
    }

    public String getFileHash() {
        return fileHash;
    }

    public ImportJobStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Optional<Instant> getDeletedAt() {
        return Optional.ofNullable(deletedAt);
    }

    private void setFileRef(final String fileRef) {
        this.fileRef = this.assertArgumentNotEmpty(fileRef, "fileRef", "should not be empty");
    }

    private void setFileHash(final String fileHash) {
        this.fileHash = this.assertArgumentNotEmpty(fileHash, "fileHash", "should not be empty");
    }

    private void setStatus(final ImportJobStatus status) {
        this.status = this.assertArgumentNotNull(status, "status", "should not be null");
    }

    private void setCreatedAt(final Instant createdAt) {
        this.createdAt = this.assertArgumentNotNull(createdAt, "createdAt", "should not be null");
    }

    private void setUpdatedAt(final Instant updatedAt) {
        this.updatedAt = this.assertArgumentNotNull(updatedAt, "updatedAt", "should not be null");
    }

    private void setDeletedAt(final Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    @Override
    public void validate(ValidationHandler aHandler) {
    }

    @Override
    public String toString() {
        return "ImportJob(" +
                "fileRef='" + fileRef + '\'' +
                ", fileHash='" + fileHash + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", deletedAt=" + deletedAt +
                ')';
    }
}
