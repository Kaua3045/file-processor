package com.kaua.file.processor.infrastructure.jobs;

import com.kaua.file.processor.domain.events.DomainEvent;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import com.kaua.file.processor.infrastructure.outbox.OutboxRepository;
import com.kaua.file.processor.infrastructure.outbox.OutboxStatus;
import com.kaua.file.processor.infrastructure.services.eventbus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Profile({"!test-integration", "!test-integration-kafka"})
public class PublishOutboxEventsJob {

    private static final Logger log = LoggerFactory.getLogger(PublishOutboxEventsJob.class);

    private final OutboxRepository outboxRepository;
    private final EventBus eventBus;

    public PublishOutboxEventsJob(
            final OutboxRepository outboxRepository,
            final EventBus eventBus
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository);
        this.eventBus = Objects.requireNonNull(eventBus);
    }

    @Scheduled(fixedRate = 5000)
    public void publish() {
        log.info("Starting publish outbox events job");

        final var aEvents = this.outboxRepository.outboxOfStatusAndOccurredOn(OutboxStatus.PENDING);

        log.info("Found {} unpublished events", aEvents.size());

        aEvents.forEach(event -> {
            try {
                final var aDomainEvent = (DomainEvent) Json.readTree(event.payload(), Class.forName(event.eventClass()));
                eventBus.publish(aDomainEvent);
                outboxRepository.markAsCompleted(event);
                log.info("Successfully published event with id {}", event.eventId());
            } catch (final Exception e) {
                log.error("Failed to publish event with id {}", event.eventId(), e);
                this.outboxRepository.markAsFailed(event);
            }
        });

        log.info("Finished publish outbox events job");
    }
}
