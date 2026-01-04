package com.kaua.file.processor.infrastructure.outbox;

import java.time.Instant;

public record OutboxEntity(
        String eventId,
        String aggregateId,
        String eventType,
        long version,
        OutboxStatus status,
        String payload,
        Instant occurredOn,
        String eventClass
) {
}
