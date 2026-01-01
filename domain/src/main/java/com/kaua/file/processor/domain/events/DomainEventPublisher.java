package com.kaua.file.processor.domain.events;

@FunctionalInterface
public interface DomainEventPublisher {

    <T extends DomainEvent> void publish(T event);
}
