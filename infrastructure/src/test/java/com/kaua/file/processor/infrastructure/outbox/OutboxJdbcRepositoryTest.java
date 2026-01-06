package com.kaua.file.processor.infrastructure.outbox;

import com.kaua.file.processor.AbstractRepositoryTest;
import com.kaua.file.processor.domain.events.DomainEvent;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.domain.utils.InstantUtils;
import com.kaua.file.processor.domain.utils.ULID;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

class OutboxJdbcRepositoryTest extends AbstractRepositoryTest {

    @Test
    void assertDependencies() {
        Assertions.assertNotNull(outboxRepository());
    }

    @Test
    void givenAValidOutboxEntity_whenCallsSave_shouldPersistIt() {
        Assertions.assertEquals(0, countOutboxEvents());
        final var aAggregateId = IdentifierUtils.generateNewULID().toString();
        final var aDomainEvent = new FakeDomainEvent(
                aAggregateId,
                1L
        );

        final var aOutboxEntity = new OutboxEntity(
                ULID.random().toString(),
                aAggregateId,
                aDomainEvent.eventType(),
                aDomainEvent.aggregateVersion(),
                OutboxStatus.PENDING,
                Json.writeValueAsString(aDomainEvent),
                aDomainEvent.occurredOn(),
                aDomainEvent.getClass().getCanonicalName()
        );

        final var savedOutboxEntity = this.outboxRepository().save(aOutboxEntity);

        Assertions.assertEquals(1, countOutboxEvents());

        Assertions.assertEquals(aOutboxEntity.eventId(), savedOutboxEntity.eventId());
        Assertions.assertEquals(aOutboxEntity.aggregateId(), savedOutboxEntity.aggregateId());
        Assertions.assertEquals(aOutboxEntity.eventType(), savedOutboxEntity.eventType());
        Assertions.assertEquals(aOutboxEntity.version(), savedOutboxEntity.version());
        Assertions.assertEquals(aOutboxEntity.status(), savedOutboxEntity.status());
        Assertions.assertEquals(aOutboxEntity.payload(), savedOutboxEntity.payload());
        Assertions.assertEquals(aOutboxEntity.occurredOn(), savedOutboxEntity.occurredOn());
        Assertions.assertEquals(aOutboxEntity.eventClass(), savedOutboxEntity.eventClass());
    }

    @Test
    void givenAValidOutboxStatusIsPending_whenCallsOutboxOfStatusAndOccurredOn_shouldReturnOutboxEntities() {
        Assertions.assertEquals(0, countOutboxEvents());
        final var aAggregateId = IdentifierUtils.generateNewULID().toString();
        final var aDomainEvent = new FakeDomainEvent(
                aAggregateId,
                1L
        );

        final var aOutboxEntity = new OutboxEntity(
                ULID.random().toString(),
                aAggregateId,
                aDomainEvent.eventType(),
                aDomainEvent.aggregateVersion(),
                OutboxStatus.PENDING,
                Json.writeValueAsString(aDomainEvent),
                aDomainEvent.occurredOn(),
                aDomainEvent.getClass().getCanonicalName()
        );

        this.outboxRepository().save(aOutboxEntity);

        final var aOutboxEntityFailed = new OutboxEntity(
                ULID.random().toString(),
                aAggregateId,
                aDomainEvent.eventType(),
                aDomainEvent.aggregateVersion(),
                OutboxStatus.FAILED,
                Json.writeValueAsString(aDomainEvent),
                aDomainEvent.occurredOn(),
                aDomainEvent.getClass().getCanonicalName()
        );

        this.outboxRepository().save(aOutboxEntityFailed);

        Assertions.assertEquals(2, countOutboxEvents());

        final var outboxEntities = this.outboxRepository().outboxOfStatusAndOccurredOn(OutboxStatus.PENDING);

        Assertions.assertEquals(1, outboxEntities.size());
        final var retrievedOutboxEntity = outboxEntities.getFirst();

        Assertions.assertEquals(aOutboxEntity.eventId(), retrievedOutboxEntity.eventId());
        Assertions.assertEquals(aOutboxEntity.aggregateId(), retrievedOutboxEntity.aggregateId());
        Assertions.assertEquals(aOutboxEntity.eventType(), retrievedOutboxEntity.eventType());
        Assertions.assertEquals(aOutboxEntity.version(), retrievedOutboxEntity.version());
        Assertions.assertEquals(aOutboxEntity.status(), retrievedOutboxEntity.status());
        Assertions.assertEquals(aOutboxEntity.payload(), retrievedOutboxEntity.payload());
        Assertions.assertEquals(aOutboxEntity.occurredOn(), retrievedOutboxEntity.occurredOn());
        Assertions.assertEquals(aOutboxEntity.eventClass(), retrievedOutboxEntity.eventClass());
    }

    @Test
    void givenAValidOutboxEntity_whenCallsMarkAsCompleted_shouldUpdateItsStatus() {
        Assertions.assertEquals(0, countOutboxEvents());
        final var aAggregateId = IdentifierUtils.generateNewULID().toString();
        final var aDomainEvent = new FakeDomainEvent(
                aAggregateId,
                1L
        );

        final var aOutboxEntity = new OutboxEntity(
                ULID.random().toString(),
                aAggregateId,
                aDomainEvent.eventType(),
                aDomainEvent.aggregateVersion(),
                OutboxStatus.PENDING,
                Json.writeValueAsString(aDomainEvent),
                aDomainEvent.occurredOn(),
                aDomainEvent.getClass().getCanonicalName()
        );

        this.outboxRepository().save(aOutboxEntity);

        Assertions.assertEquals(1, countOutboxEvents());

        final var completedOutboxEntity = this.outboxRepository().markAsCompleted(aOutboxEntity);

        Assertions.assertEquals(OutboxStatus.COMPLETED, completedOutboxEntity.status());
    }

    @Test
    void givenAValidOutboxEntity_whenCallsMarkAsFailed_shouldUpdateItsStatus() {
        Assertions.assertEquals(0, countOutboxEvents());
        final var aAggregateId = IdentifierUtils.generateNewULID().toString();
        final var aDomainEvent = new FakeDomainEvent(
                aAggregateId,
                1L
        );

        final var aOutboxEntity = new OutboxEntity(
                ULID.random().toString(),
                aAggregateId,
                aDomainEvent.eventType(),
                aDomainEvent.aggregateVersion(),
                OutboxStatus.PENDING,
                Json.writeValueAsString(aDomainEvent),
                aDomainEvent.occurredOn(),
                aDomainEvent.getClass().getCanonicalName()
        );

        this.outboxRepository().save(aOutboxEntity);

        Assertions.assertEquals(1, countOutboxEvents());

        final var failedOutboxEntity = this.outboxRepository().markAsFailed(aOutboxEntity);

        Assertions.assertEquals(OutboxStatus.FAILED, failedOutboxEntity.status());
    }

    private record FakeDomainEvent(
            String eventId,
            String eventType,
            Instant occurredOn,
            String aggregateId,
            long aggregateVersion,
            String source,
            String traceId
    ) implements DomainEvent {

        public FakeDomainEvent(
                String aggregateId,
                long aggregateVersion
        ) {
            this(
                    IdentifierUtils.generateNewULID().toString(),
                    "FakeDomainEvent",
                    InstantUtils.now(),
                    aggregateId,
                    aggregateVersion,
                    "FileProcessorService",
                    IdentifierUtils.generateNewULID().toString()
            );
        }
    }
}
