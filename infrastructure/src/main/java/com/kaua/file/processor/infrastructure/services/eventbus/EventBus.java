package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.infrastructure.outbox.OutboxEntity;

public interface EventBus {

    void publish(OutboxEntity outbox);
}
