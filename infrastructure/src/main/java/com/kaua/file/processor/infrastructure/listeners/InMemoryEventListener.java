package com.kaua.file.processor.infrastructure.listeners;

import com.kaua.file.processor.domain.events.DomainEvent;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "application.eventbus", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventListener.class);

    @EventListener
    public void handleEvents(DomainEvent aDomainEvent) {
        log.info("DomainEvent received: {}", aDomainEvent);
        switch (aDomainEvent.eventType()) {
            case "ImportJobCreated" -> this.handleImportJobCreatedEvent((ImportJobCreatedEvent) aDomainEvent);
            default -> throw new IllegalArgumentException("Event type not recognized: " + aDomainEvent.eventType());
        }
    }

    private void handleImportJobCreatedEvent(final ImportJobCreatedEvent importJobCreatedEvent) {
        log.info("Import Job Event received: {}", importJobCreatedEvent);
    }
}
