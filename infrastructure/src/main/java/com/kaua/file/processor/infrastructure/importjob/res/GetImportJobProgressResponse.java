package com.kaua.file.processor.infrastructure.importjob.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.file.processor.application.importjob.progress.GetImportJobProgressOutput;

import java.time.Instant;

public record GetImportJobProgressResponse(
        @JsonProperty("import_job_id") String importJobId,
        @JsonProperty("version") long version,
        @JsonProperty("file_ref") String fileRef,
        @JsonProperty("file_hash") String fileHash,
        @JsonProperty("status") String status,
        @JsonProperty("created_at") Instant createdAt,
        @JsonProperty("updated_at") Instant updatedAt,
        @JsonProperty("deleted_at") Instant deletedAt,
        @JsonProperty("processed_rows") long processedRows
) {

    public static GetImportJobProgressResponse from(final GetImportJobProgressOutput aImportJob) {
        return new GetImportJobProgressResponse(
                aImportJob.importJobId(),
                aImportJob.version(),
                aImportJob.fileRef(),
                aImportJob.fileHash(),
                aImportJob.status(),
                aImportJob.createdAt(),
                aImportJob.updatedAt(),
                aImportJob.deletedAt(),
                aImportJob.processedRows()
        );
    }
}
