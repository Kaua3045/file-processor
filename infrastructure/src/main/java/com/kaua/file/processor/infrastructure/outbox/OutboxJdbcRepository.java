package com.kaua.file.processor.infrastructure.outbox;

import com.kaua.file.processor.infrastructure.jdbc.DatabaseClient;
import com.kaua.file.processor.infrastructure.jdbc.JdbcUtils;
import com.kaua.file.processor.infrastructure.jdbc.RowMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
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

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OutboxEntity markAsCompleted(final OutboxEntity entity) {
        log.info("Marking outbox event `{}` as COMPLETED", entity.eventId());

        final var aSql = """
                UPDATE outbox SET status = :status WHERE id = :id
                """;

        final var aCompletedOutboxEntity = new OutboxEntity(
                entity.eventId(),
                entity.aggregateId(),
                entity.eventType(),
                entity.version(),
                OutboxStatus.COMPLETED,
                entity.payload(),
                entity.occurredOn()
        );

        final var aParams = new HashMap<String, Object>();
        aParams.put("status", aCompletedOutboxEntity.status().name());
        aParams.put("id", aCompletedOutboxEntity.eventId());

        this.databaseClient.update(aSql, aParams);
        log.info("Outbox event `{}` marked as COMPLETED", entity.eventId());
        return aCompletedOutboxEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OutboxEntity markAsFailed(final OutboxEntity entity) {
        log.info("Marking outbox event `{}` as FAILED", entity.eventId());
        final var aSql = """
                UPDATE outbox SET status = :status WHERE id = :id
                """;

        final var aFailedOutboxEntity = new OutboxEntity(
                entity.eventId(),
                entity.aggregateId(),
                entity.eventType(),
                entity.version(),
                OutboxStatus.FAILED,
                entity.payload(),
                entity.occurredOn()
        );

        final var aParams = new HashMap<String, Object>();
        aParams.put("status", aFailedOutboxEntity.status().name());
        aParams.put("id", aFailedOutboxEntity.eventId());

        this.databaseClient.update(aSql, aParams);
        log.info("Outbox event `{}` marked as FAILED", entity.eventId());
        return aFailedOutboxEntity;
    }

    @Transactional
    @Override
    public List<OutboxEntity> outboxOfStatusAndOccurredOn(final OutboxStatus status) {
        log.info("Retrieving outbox events with status `{}`", status.name());
        final var aSql = """
                SELECT * FROM outbox WHERE status = :status ORDER BY occurred_on LIMIT 100 FOR UPDATE SKIP LOCKED
                """;

        final var aParams = new HashMap<String, Object>();
        aParams.put("status", status.name());

        final var aEntities = this.databaseClient.query(aSql, aParams, outboxMapper());
        log.info("Retrieved {} outbox events with status `{}`", aEntities.size(), status.name());
        return aEntities;
    }

    private RowMap<OutboxEntity> outboxMapper() {
        return rs -> new OutboxEntity(
                rs.getString("id"),
                rs.getString("aggregate_id"),
                rs.getString("event_type"),
                rs.getInt("version"),
                OutboxStatus.valueOf(rs.getString("status")),
                rs.getString("payload"),
                JdbcUtils.getInstant(rs, "occurred_on")
        );
    }
}
