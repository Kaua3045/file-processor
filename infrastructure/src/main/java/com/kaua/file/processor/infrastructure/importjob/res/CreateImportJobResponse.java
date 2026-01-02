package com.kaua.file.processor.infrastructure.importjob.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kaua.file.processor.application.importjob.create.CreateImportJobOutput;

public record CreateImportJobResponse(
        @JsonProperty("import_job_id") String importJobId,
        @JsonProperty("file_name") String fileName,
        @JsonProperty("status") String status
) {

    public static CreateImportJobResponse from(final CreateImportJobOutput aOutput) {
        return new CreateImportJobResponse(
                aOutput.importJobId(),
                aOutput.fileName(),
                aOutput.status()
        );
    }
}
