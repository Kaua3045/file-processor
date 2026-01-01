package com.kaua.file.processor.domain.importjob;

import com.kaua.file.processor.domain.Identifier;
import com.kaua.file.processor.domain.utils.ULID;

public record ImportJobId(ULID value) implements Identifier<ULID> {

    public ImportJobId {
        this.assertArgumentNotNull(value, "id", "should not be null");
    }
}
