package com.kaua.file.processor.infrastructure.processedEvents;

import com.kaua.file.processor.domain.utils.InstantUtils;
import com.kaua.file.processor.infrastructure.jdbc.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class ProcessedEventJdbcRepository implements ProcessedEventRepository {

    private static final Logger log = LoggerFactory.getLogger(ProcessedEventJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public ProcessedEventJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Transactional
    @Override
    public void save(final String eventId) {
        log.info("Saving processed event with ID: {}", eventId);

        final var aSql = "INSERT INTO processed_events (event_id, processed_at) VALUES (:eventId, :processedAt)";
        Map<String, Object> aParams = new HashMap<>();
        aParams.put("eventId", eventId);
        aParams.put("processedAt", InstantUtils.now());

        this.databaseClient.update(aSql, aParams);
        log.info("Processed event with ID: {} saved successfully", eventId);
    }

    @Override
    public boolean existsById(final String eventId) {
        log.info("Checking existence of processed event with ID: {}", eventId);

        final var aSql = "SELECT COUNT(*) FROM processed_events WHERE event_id = :eventId";

        final var aExists = this.databaseClient.count(aSql, Map.of("eventId", eventId)) > 0;
        log.info("Processed event with ID: {} exists: {}", eventId, aExists);
        return aExists;
    }
}
