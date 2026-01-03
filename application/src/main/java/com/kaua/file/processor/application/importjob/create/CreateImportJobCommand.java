package com.kaua.file.processor.application.importjob.create;

import java.io.InputStream;

public record CreateImportJobCommand(
        InputStream fileContent,
        String fileName
) {

    public static CreateImportJobCommand with(
            final InputStream aFileContent,
            final String aFileName
    ) {
        return new CreateImportJobCommand(aFileContent, aFileName);
    }
}
