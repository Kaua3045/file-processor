package com.kaua.file.processor.application.importjob.progress;

import com.kaua.file.processor.domain.importjob.ImportJob;

import java.time.Instant;

public record GetImportJobProgressOutput(
        String importJobId,
        long version,
        String fileRef,
        String fileHash,
        String status,
        Instant createdAt,
        Instant updatedAt,
        Instant deletedAt,
        long processedRows
) {

    public static GetImportJobProgressOutput from(final ImportJob aImportJob) {
        return new GetImportJobProgressOutput(
                aImportJob.getId().value().toString(),
                aImportJob.getVersion(),
                aImportJob.getFileRef(),
                aImportJob.getFileHash(),
                aImportJob.getStatus().name(),
                aImportJob.getCreatedAt(),
                aImportJob.getUpdatedAt(),
                aImportJob.getDeletedAt().orElse(null),
                aImportJob.getProcessedRows()
        );
    }
}
