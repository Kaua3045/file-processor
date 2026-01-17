package com.kaua.file.processor.infrastructure.listeners;

import com.kaua.file.processor.application.importjob.process.ProcessImportJobCommand;
import com.kaua.file.processor.application.importjob.process.ProcessImportJobUseCase;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import com.kaua.file.processor.infrastructure.outbox.OutboxEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@ConditionalOnProperty(name = "application.eventbus", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InMemoryEventListener.class);

    private final ProcessImportJobUseCase processImportJobUseCase;

    public InMemoryEventListener(
            final ProcessImportJobUseCase processImportJobUseCase
    ) {
        this.processImportJobUseCase = Objects.requireNonNull(processImportJobUseCase);
    }

    @EventListener
    public void handleEvents(OutboxEntity outboxEntity) {
        log.info("DomainEvent received: {}", outboxEntity);
        switch (outboxEntity.eventType()) {
            case "ImportJobCreated" ->
                    this.handleImportJobCreatedEvent(Json.readValue(outboxEntity.payload(), ImportJobCreatedEvent.class));
            default -> throw new IllegalArgumentException("Event type not recognized: " + outboxEntity.eventType());
        }
    }

    private void handleImportJobCreatedEvent(final ImportJobCreatedEvent importJobCreatedEvent) {
        log.info("Import Job Event received: {}", importJobCreatedEvent);
        this.processImportJobUseCase.execute(ProcessImportJobCommand.with(importJobCreatedEvent.aggregateId()));
        log.info("Import Job processed: {}", importJobCreatedEvent.aggregateId());
    }
}
