package com.kaua.file.processor.infrastructure.repositories;

import com.kaua.file.processor.application.repository.ImportJobRepository;
import com.kaua.file.processor.domain.importjob.ImportJob;
import com.kaua.file.processor.domain.importjob.ImportJobId;
import com.kaua.file.processor.domain.importjob.ImportJobStatus;
import com.kaua.file.processor.domain.utils.ULID;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import com.kaua.file.processor.infrastructure.jdbc.DatabaseClient;
import com.kaua.file.processor.infrastructure.jdbc.JdbcUtils;
import com.kaua.file.processor.infrastructure.jdbc.RowMap;
import com.kaua.file.processor.infrastructure.outbox.OutboxEntity;
import com.kaua.file.processor.infrastructure.outbox.OutboxRepository;
import com.kaua.file.processor.infrastructure.outbox.OutboxStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Component
public class ImportJobJdbcRepository implements ImportJobRepository {

    private static final Logger log = LoggerFactory.getLogger(ImportJobJdbcRepository.class);

    private final DatabaseClient databaseClient;
    private final OutboxRepository outboxRepository;

    public ImportJobJdbcRepository(
            final DatabaseClient databaseClient,
            final OutboxRepository outboxRepository
    ) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
        this.outboxRepository = Objects.requireNonNull(outboxRepository);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public ImportJob save(final ImportJob importJob) {
        if (importJob.getVersion() == 0) {
            log.info("Inserting new import job with id `{}`", importJob.getId());
            create(importJob);
            importJob.getDomainEvents().forEach(it ->
                    this.outboxRepository.save(new OutboxEntity(
                            it.eventId(),
                            it.aggregateId(),
                            it.eventType(),
                            it.aggregateVersion(),
                            OutboxStatus.PENDING,
                            Json.writeValueAsString(it),
                            it.occurredOn()
                    ))
            );
            log.info("Import job with id `{}` inserted successfully", importJob.getId());
        }

        importJob.incrementVersion();
        return importJob;
    }

    @Override
    public Optional<ImportJob> importJobOfFileHash(final String fileHash) {
        final var aSql = """
                SELECT * FROM import_jobs WHERE file_hash = :file_hash
                """;
        return this.databaseClient.queryOne(aSql, Map.of("file_hash", fileHash), importJobMapper());
    }

    private void create(final ImportJob aImportJob) {
        final var aSql = """
                INSERT INTO import_jobs (id, file_ref, file_hash, status, created_at, updated_at, deleted_at, version)
                VALUES (:id, :file_ref, :file_hash, :status, :created_at, :updated_at, :deleted_at, (:version + 1))
                """;

        executeUpdate(aSql, aImportJob);
    }

    private int executeUpdate(final String aSql, final ImportJob aImportJob) {
        final var aParams = new HashMap<String, Object>();
        aParams.put("id", aImportJob.getId().value().toString());
        aParams.put("file_ref", aImportJob.getFileRef());
        aParams.put("file_hash", aImportJob.getFileHash());
        aParams.put("status", aImportJob.getStatus().name());
        aParams.put("created_at", aImportJob.getCreatedAt());
        aParams.put("updated_at", aImportJob.getUpdatedAt());
        aParams.put("deleted_at", aImportJob.getDeletedAt().orElse(null));
        aParams.put("version", aImportJob.getVersion());

        return this.databaseClient.update(aSql, aParams);
    }

    private RowMap<ImportJob> importJobMapper() {
        return rs -> ImportJob.with(
                new ImportJobId(ULID.fromString(rs.getString("id"))),
                rs.getLong("version"),
                rs.getString("file_ref"),
                rs.getString("file_hash"),
                ImportJobStatus.from(rs.getString("status")).orElse(null),
                JdbcUtils.getInstant(rs, "created_at"),
                JdbcUtils.getInstant(rs, "updated_at"),
                JdbcUtils.getInstant(rs, "deleted_at")
        );
    }
}
