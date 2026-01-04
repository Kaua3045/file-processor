package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.domain.events.DomainEvent;

public interface EventBus {

    void publish(DomainEvent event);
}
