package com.kaua.file.processor.infrastructure.outbox;

import java.time.Instant;

public record OutboxEntity(
        String eventId,
        String aggregateId,
        String eventType,
        long version,
        OutboxStatus status,
        byte[] payload,
        Instant occurredOn,
        OutboxPayloadType payloadType
) {
}
