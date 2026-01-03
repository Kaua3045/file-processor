package com.kaua.file.processor.infrastructure.outbox;

import com.kaua.file.processor.infrastructure.jdbc.DatabaseClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Objects;

@Component
public class OutboxJdbcRepository implements OutboxRepository {

    private static final Logger log = LoggerFactory.getLogger(OutboxJdbcRepository.class);

    private final DatabaseClient databaseClient;

    public OutboxJdbcRepository(final DatabaseClient databaseClient) {
        this.databaseClient = Objects.requireNonNull(databaseClient);
    }

    @Override
    public OutboxEntity save(final OutboxEntity entity) {
        log.info("Saving outbox event `{}` with event type `{}` for aggregate `{}`",
                entity.eventId(),
                entity.eventType(),
                entity.aggregateId()
        );

        final var aSql = """
                INSERT INTO outbox (id, aggregate_id, event_type, version, status, payload, occurred_on)
                VALUES (:id, :aggregateId, :eventType, :version, :status, :payload, :occurredOn)
                """;

        final var aParams = new HashMap<String, Object>();
        aParams.put("id", entity.eventId());
        aParams.put("aggregateId", entity.aggregateId());
        aParams.put("eventType", entity.eventType());
        aParams.put("version", entity.version());
        aParams.put("status", entity.status().name());
        aParams.put("payload", entity.payload());
        aParams.put("occurredOn", entity.occurredOn());

        this.databaseClient.update(aSql, aParams);

        log.info("Outbox event `{}` with event type `{}` saved for aggregate `{}`", entity.eventId(), entity.eventType(), entity.aggregateId());

        return entity;
    }
}
