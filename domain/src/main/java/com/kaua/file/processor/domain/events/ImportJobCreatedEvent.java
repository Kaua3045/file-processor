package com.kaua.file.processor.domain.events;

import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.domain.utils.InstantUtils;

import java.time.Instant;

public record ImportJobCreatedEvent(
        String eventId,
        String eventType,
        Instant occurredOn,
        String aggregateId,
        long aggregateVersion,
        String source,
        String traceId
) implements DomainEvent {

    public ImportJobCreatedEvent(
            String aggregateId,
            long aggregateVersion
    ) {
        this(
                IdentifierUtils.generateNewULID().toString(),
                "ImportJobCreated",
                InstantUtils.now(),
                aggregateId,
                aggregateVersion,
                "FileProcessorService",
                IdentifierUtils.generateNewULID().toString()
        );
    }
}
