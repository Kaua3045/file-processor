package com.kaua.file.processor.application.repository;

import com.kaua.file.processor.domain.importjob.StoredFile;

import java.io.InputStream;

public interface FileStorageRepository {

    StoredFile store(String fileName, InputStream content);

    void delete(String fileRef);

    InputStream load(String fileRef);
}
