package com.kaua.file.processor.application.importjob.process;

public record ProcessImportJobCommand(
        String importJobId
) {

    public static ProcessImportJobCommand with(final String importJobId) {
        return new ProcessImportJobCommand(importJobId);
    }
}
