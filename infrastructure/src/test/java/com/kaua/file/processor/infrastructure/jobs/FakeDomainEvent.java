package com.kaua.file.processor.infrastructure.jobs;

import com.kaua.file.processor.domain.events.DomainEvent;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.domain.utils.InstantUtils;

import java.time.Instant;

public record FakeDomainEvent(
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
