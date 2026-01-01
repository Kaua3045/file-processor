package com.kaua.file.processor.domain.importjob;

import java.util.Arrays;
import java.util.Optional;

public enum ImportJobStatus {

    CREATED,
    QUEUED,
    PROCESSING,
    COMPLETED,
    FAILED,
    DELETED;

    public static Optional<ImportJobStatus> from(final String aStatus) {
        return Arrays.stream(values())
                .filter(it -> it.name().equalsIgnoreCase(aStatus))
                .findFirst();
    }
}
