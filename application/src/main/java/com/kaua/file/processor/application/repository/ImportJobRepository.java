package com.kaua.file.processor.application.repository;

import com.kaua.file.processor.domain.importjob.ImportJob;

import java.util.Optional;

public interface ImportJobRepository {

    ImportJob save(ImportJob importJob);

    Optional<ImportJob> importJobOfFileHash(String fileHash);
}
