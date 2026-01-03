package com.kaua.file.processor.infrastructure.outbox;

import java.util.List;

public interface OutboxRepository {

    OutboxEntity save(OutboxEntity entity);

    OutboxEntity markAsCompleted(OutboxEntity entity);

    OutboxEntity markAsFailed(OutboxEntity entity);

    List<OutboxEntity> outboxOfStatusAndOccurredOn(OutboxStatus status);
}
