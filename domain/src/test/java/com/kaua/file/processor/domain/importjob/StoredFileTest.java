package com.kaua.file.processor.domain.importjob;

import com.kaua.file.processor.domain.UnitTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StoredFileTest extends UnitTest {

    @Test
    void givenAValidValues_whenCallsNewStoredFile_thenCreated() {
        final var fileRef = "file-ref";
        final var sha256 = "sha256-hash";
        final var size = 1024L;

        final var storedFile = new StoredFile(
                fileRef,
                sha256,
                size
        );

        assertEquals(fileRef, storedFile.fileRef());
        assertEquals(sha256, storedFile.sha256());
        assertEquals(size, storedFile.size());
    }
}
