package com.kaua.file.processor.domain.importjob;

import com.kaua.file.processor.domain.ValueObject;

public record StoredFile(
        String fileRef,
        String sha256,
        long size
) implements ValueObject {
}
