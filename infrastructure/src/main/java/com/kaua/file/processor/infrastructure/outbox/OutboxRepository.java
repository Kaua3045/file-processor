package com.kaua.file.processor.infrastructure.outbox;

public interface OutboxRepository {

    OutboxEntity save(OutboxEntity entity);
}
