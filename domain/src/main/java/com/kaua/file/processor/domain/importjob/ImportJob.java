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
    private long processedRows;

    private ImportJob(
            final ImportJobId aImportJobId,
            final long aVersion,
            final String aFileRef,
            final String aFileHash,
            final ImportJobStatus aStatus,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aDeletedAt,
            final long aProcessedRows
    ) {
        super(aImportJobId, aVersion);
        setFileRef(aFileRef);
        setFileHash(aFileHash);
        setStatus(aStatus);
        setCreatedAt(aCreatedAt);
        setUpdatedAt(aUpdatedAt);
        setDeletedAt(aDeletedAt);
        setProcessedRows(aProcessedRows);
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
                null,
                0L
        );
    }

    public static ImportJob with(
            final ImportJobId aId,
            final long aVersion,
            final String aFileRef,
            final String aFileHash,
            final ImportJobStatus aStatus,
            final Instant aCreatedAt,
            final Instant aUpdatedAt,
            final Instant aDeletedAt,
            final long aProcessedRows
    ) {
        return new ImportJob(
                aId,
                aVersion,
                aFileRef,
                aFileHash,
                aStatus,
                aCreatedAt,
                aUpdatedAt,
                aDeletedAt,
                aProcessedRows
        );
    }

    public void start() {
        this.setStatus(ImportJobStatus.PROCESSING);
        this.setUpdatedAt(InstantUtils.now());
    }

    public void complete() {
        this.setStatus(ImportJobStatus.COMPLETED);
        this.setUpdatedAt(InstantUtils.now());
    }

    public void fail() {
        this.setStatus(ImportJobStatus.FAILED);
        this.setUpdatedAt(InstantUtils.now());
    }

    public void incrementProcessed(final long count) {
        this.processedRows += count;
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

    public long getProcessedRows() {
        return processedRows;
    }

    private void setFileRef(final String fileRef) {
        this.assertArgumentNotEmpty(fileRef, "fileRef", "should not be empty");
        this.assertArgumentMaxLength(fileRef, 255, "fileRef", "must be less than 255 characters");
        this.fileRef = fileRef;
    }

    private void setFileHash(final String fileHash) {
        this.assertArgumentNotEmpty(fileHash, "fileHash", "should not be empty");
        this.assertArgumentMaxLength(fileHash, 64, "fileHash", "must be less than 64 characters");
        this.fileHash = fileHash;
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

    private void setProcessedRows(final long processedRows) {
        this.processedRows = processedRows;
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
                ", processedRows=" + processedRows +
                ')';
    }
}
