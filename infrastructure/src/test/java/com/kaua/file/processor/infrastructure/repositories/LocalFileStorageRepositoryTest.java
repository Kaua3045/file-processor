package com.kaua.file.processor.infrastructure.repositories;

import com.kaua.file.processor.domain.importjob.StoredFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileStorageRepositoryTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldStoreFileSuccessfully() {
        var repository = new LocalFileStorageRepository(tempDir);
        var content = "hello world";
        var inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        StoredFile stored = repository.store("test.txt", inputStream);

        Path storedPath = Path.of(stored.fileRef());

        assertTrue(Files.exists(storedPath));
        assertEquals(content.length(), stored.size());
        assertTrue(storedPath.startsWith(tempDir));
    }

    @Test
    void shouldCalculateCorrectSha256Hash() throws Exception {
        var repository = new LocalFileStorageRepository(tempDir);
        var content = "hash-test";
        var inputStream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));

        StoredFile stored = repository.store("file.txt", inputStream);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] expectedHashBytes = digest.digest(content.getBytes(StandardCharsets.UTF_8));
        String expectedHash = HexFormat.of().formatHex(expectedHashBytes);

        assertEquals(expectedHash, stored.sha256());
    }

    @Test
    void shouldDeleteStoredFile() {
        var repository = new LocalFileStorageRepository(tempDir);
        var inputStream = new ByteArrayInputStream("delete-me".getBytes());

        StoredFile stored = repository.store("delete.txt", inputStream);
        Path storedPath = Path.of(stored.fileRef());

        assertTrue(Files.exists(storedPath));

        repository.delete(stored.fileRef());

        assertFalse(Files.exists(storedPath));
    }

    @Test
    void deleteShouldBeIdempotent() {
        var repository = new LocalFileStorageRepository(tempDir);

        assertDoesNotThrow(() ->
                repository.delete(tempDir.resolve("non-existent.txt").toString())
        );
    }

    @Test
    void shouldNotDeleteFileOutsideBaseDir() throws Exception {
        var repository = new LocalFileStorageRepository(tempDir);

        Path outsideFile = Files.createTempFile("outside", ".txt");
        Files.writeString(outsideFile, "secret");

        try {
            assertDoesNotThrow(() ->
                    repository.delete(outsideFile.toString())
            );

            assertTrue(Files.exists(outsideFile));

        } finally {
            Files.deleteIfExists(outsideFile);
        }
    }

    @Test
    void shouldSanitizeFileName() throws Exception {
        var repository = new LocalFileStorageRepository(tempDir);
        var inputStream = new ByteArrayInputStream("content".getBytes());

        StoredFile stored = repository.store("inv@lid n@me!!.txt", inputStream);

        Path storedPath = Path.of(stored.fileRef());
        String fileName = storedPath.getFileName().toString();

        assertFalse(fileName.contains("@"));
        assertFalse(fileName.contains(" "));
        assertTrue(fileName.endsWith(".txt"));
    }
}
