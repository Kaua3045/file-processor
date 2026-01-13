package com.kaua.file.processor.application.importjob.progress;

public record GetImportJobProgressCommand(
        String importJobId
) {

    public static GetImportJobProgressCommand with(final String importJobId) {
        return new GetImportJobProgressCommand(importJobId);
    }
}
