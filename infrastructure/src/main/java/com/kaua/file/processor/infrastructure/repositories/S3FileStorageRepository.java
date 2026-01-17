package com.kaua.file.processor.infrastructure.repositories;

import com.kaua.file.processor.application.repository.FileStorageRepository;
import com.kaua.file.processor.domain.exceptions.InternalErrorException;
import com.kaua.file.processor.domain.importjob.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;

public class S3FileStorageRepository implements FileStorageRepository {

    private static final Logger log = LoggerFactory.getLogger(S3FileStorageRepository.class);

    private final S3Client s3Client;
    private final String bucket;

    public S3FileStorageRepository(final S3Client s3Client, final String bucket) {
        this.s3Client = Objects.requireNonNull(s3Client);
        this.bucket = bucket;
    }

    @Override
    public StoredFile store(final String fileName, final InputStream content) {
        try {
            log.info("Storing file `{}` in S3 bucket `{}`", fileName, bucket);

            final byte[] bytes = content.readAllBytes();

            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final String hash = HexFormat.of().formatHex(digest.digest(bytes));

            final long size = bytes.length;

            final String key = createKey(fileName);

            final PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            this.s3Client.putObject(
                    request,
                    RequestBody.fromBytes(bytes)
            );

            log.info(
                    "File `{}` stored in S3 with key `{}`, hash `{}`, size `{}` bytes",
                    fileName,
                    key,
                    hash,
                    size
            );

            return new StoredFile(key, hash, size);

        } catch (Exception ex) {
            log.error("Error storing file `{}` in S3", fileName, ex);
            throw InternalErrorException.with(
                    "Error storing file `%s` in S3".formatted(fileName)
            );
        }
    }

    @Override
    public void delete(final String fileRef) {
        try {
            log.info("Deleting S3 object `{}` from bucket `{}`", fileRef, bucket);

            final var aRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileRef)
                    .build();

            this.s3Client.deleteObject(aRequest);

            log.info("S3 object `{}` deleted successfully", fileRef);
        } catch (Exception ex) {
            log.error("Error deleting S3 object `{}`", fileRef, ex);
            throw InternalErrorException.with("Error deleting file `%s` from S3".formatted(fileRef));
        }
    }

    @Override
    public InputStream load(final String fileRef) {
        try {
            log.info("Loading S3 object `{}` from bucket `{}`", fileRef, bucket);

            final var aRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileRef)
                    .build();

            return this.s3Client.getObject(aRequest);
        } catch (Exception ex) {
            log.error("Error loading S3 object `{}`", fileRef, ex);
            throw InternalErrorException.with("Error loading file `%s` from S3".formatted(fileRef));
        }
    }

    private String createKey(final String originalName) {
        final var aSafeName = sanitizeFileName(originalName);
        return "uploads/%s-%s".formatted(UUID.randomUUID(), aSafeName);
    }

    private String sanitizeFileName(String original) {
        if (original == null || original.isBlank()) {
            return "file";
        }

        return original
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .toLowerCase();
    }
}
