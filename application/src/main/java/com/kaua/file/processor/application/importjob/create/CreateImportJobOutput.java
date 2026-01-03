package com.kaua.file.processor.application.importjob.create;

public record CreateImportJobOutput(
        String importJobId,
        String fileName,
        String status
) {
}
