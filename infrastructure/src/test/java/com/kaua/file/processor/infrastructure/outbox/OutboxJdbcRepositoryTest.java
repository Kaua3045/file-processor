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
                aDomainEvent.occurredOn()
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
