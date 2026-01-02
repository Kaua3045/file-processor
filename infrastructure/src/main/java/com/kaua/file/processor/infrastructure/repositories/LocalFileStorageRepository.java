package com.kaua.file.processor.infrastructure.repositories;

import com.kaua.file.processor.application.repository.FileStorageRepository;
import com.kaua.file.processor.domain.exceptions.InternalErrorException;
import com.kaua.file.processor.domain.importjob.StoredFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

public class LocalFileStorageRepository implements FileStorageRepository {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageRepository.class);

    private final Path baseDir;

    public LocalFileStorageRepository(final Path baseDir) {
        this.baseDir = baseDir;
    }

    @Override
    public StoredFile store(final String fileName, final InputStream content) {
        try {
            log.info("Storing file `{}` locally", fileName);
            final var aDigest = MessageDigest.getInstance("SHA-256");
            final var aHashingStream = new DigestInputStream(content, aDigest);

            final var aTarget = createPath(fileName);

            final var aSize = Files.copy(aHashingStream, aTarget);

            final var aHash = HexFormat.of().formatHex(aDigest.digest());

            log.info("File `{}` stored locally at `{}` with hash `{}` and size `{}` bytes",
                    fileName,
                    aTarget,
                    aHash,
                    aSize
            );

            return new StoredFile(aTarget.toString(), aHash, aSize);
        } catch (Exception ex) {
            log.error("Error storing file `{}` locally", fileName, ex);
            throw InternalErrorException.with("Error storing file `%s` locally".formatted(fileName));
        }
    }

    @Override
    public void delete(final String fileRef) {
        try {
            log.info("Deleting file at `{}`", fileRef);
            final var aPath = Paths.get(fileRef).normalize().toAbsolutePath();
            final var aBasePath = baseDir.toAbsolutePath();

            if (!aPath.startsWith(aBasePath)) {
                log.error("Attempted to delete file outside of base directory: `{}`", fileRef);
                throw InternalErrorException.with("Cannot delete file outside of base directory");
            }

            Files.deleteIfExists(aPath);
            log.info("File at `{}` deleted successfully", fileRef);
        } catch (Exception ex) {
            log.error("Error deleting file at `{}`", fileRef, ex);
            throw InternalErrorException.with("Error deleting file at `%s`".formatted(fileRef));
        }
    }

    private Path createPath(final String originalName) {
        try {
            Files.createDirectories(baseDir);

            final var aSafeName = sanitizeFileName(originalName);
            final var aFileName = UUID.randomUUID() + "-" + aSafeName;
            return baseDir.resolve(aFileName);
        } catch (Exception e) {
            log.error("Error creating file path for `{}`", originalName, e);
            throw InternalErrorException.with("Error creating file path for `%s`".formatted(originalName));
        }
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
