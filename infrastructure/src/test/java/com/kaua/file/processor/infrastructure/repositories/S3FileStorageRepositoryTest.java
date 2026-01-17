package com.kaua.file.processor.infrastructure.repositories;

import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.exceptions.InternalErrorException;
import com.kaua.file.processor.domain.importjob.StoredFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3FileStorageRepositoryTest extends UnitTest {

    @Mock
    private S3Client s3Client;

    private S3FileStorageRepository repository;

    private static final String BUCKET = "test-bucket";

    @BeforeEach
    void setUp() {
        this.repository = new S3FileStorageRepository(s3Client, BUCKET);
    }

    @Test
    void shouldStoreFileSuccessfully() throws Exception {
        byte[] contentBytes = "hello world".getBytes(StandardCharsets.UTF_8);
        InputStream inputStream = new ByteArrayInputStream(contentBytes);

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().build());

        StoredFile storedFile = repository.store("test.txt", inputStream);

        assertNotNull(storedFile);
        assertNotNull(storedFile.fileRef());
        assertTrue(storedFile.fileRef().startsWith("uploads/"));
        assertEquals(contentBytes.length, storedFile.size());

        String expectedHash = sha256Hex(contentBytes);
        assertEquals(expectedHash, storedFile.sha256());

        ArgumentCaptor<PutObjectRequest> captor =
                ArgumentCaptor.forClass(PutObjectRequest.class);

        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));

        PutObjectRequest request = captor.getValue();
        assertEquals(BUCKET, request.bucket());
        assertTrue(request.key().contains("test.txt"));
    }

    @Test
    void shouldThrowInternalErrorWhenPutFails() {
        InputStream inputStream =
                new ByteArrayInputStream("fail".getBytes(StandardCharsets.UTF_8));

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenThrow(S3Exception.builder().message("boom").build());

        InternalErrorException ex = assertThrows(
                InternalErrorException.class,
                () -> repository.store("file.txt", inputStream)
        );

        assertTrue(ex.getMessage().contains("Error storing file"));
    }

    @Test
    void shouldDeleteFileSuccessfully() {
        String key = "uploads/123-file.txt";

        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenReturn(DeleteObjectResponse.builder().build());

        repository.delete(key);

        ArgumentCaptor<DeleteObjectRequest> captor =
                ArgumentCaptor.forClass(DeleteObjectRequest.class);

        verify(s3Client).deleteObject(captor.capture());

        DeleteObjectRequest request = captor.getValue();
        assertEquals(BUCKET, request.bucket());
        assertEquals(key, request.key());
    }

    @Test
    void shouldThrowInternalErrorWhenDeleteFails() {
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("boom").build());

        InternalErrorException ex = assertThrows(
                InternalErrorException.class,
                () -> repository.delete("any-key")
        );

        assertTrue(ex.getMessage().contains("Error deleting file"));
    }

    @Test
    void shouldLoadFileSuccessfully() throws IOException {
        String key = "uploads/file.txt";
        InputStream expectedStream =
                new ByteArrayInputStream("data".getBytes(StandardCharsets.UTF_8));

        ResponseInputStream<GetObjectResponse> responseInputStream =
                new ResponseInputStream<>(
                        GetObjectResponse.builder().build(),
                        AbortableInputStream.create(
                                new ByteArrayInputStream("data".getBytes())
                        )
                );

        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenReturn(responseInputStream);

        InputStream result = repository.load(key);

        assertNotNull(result);

        byte[] bytes = result.readAllBytes();
        assertArrayEquals("data".getBytes(StandardCharsets.UTF_8), bytes);

        ArgumentCaptor<GetObjectRequest> captor =
                ArgumentCaptor.forClass(GetObjectRequest.class);

        verify(s3Client).getObject(captor.capture());

        GetObjectRequest request = captor.getValue();
        assertEquals(BUCKET, request.bucket());
        assertEquals(key, request.key());
    }

    @Test
    void shouldThrowInternalErrorWhenLoadFails() {
        when(s3Client.getObject(any(GetObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("boom").build());

        InternalErrorException ex = assertThrows(
                InternalErrorException.class,
                () -> repository.load("any-key")
        );

        assertTrue(ex.getMessage().contains("Error loading file"));
    }

    private static String sha256Hex(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(digest.digest(data));
    }
}
