package com.kaua.file.processor.infrastructure.jobs;

import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import com.kaua.file.processor.infrastructure.outbox.OutboxEntity;
import com.kaua.file.processor.infrastructure.outbox.OutboxRepository;
import com.kaua.file.processor.infrastructure.outbox.OutboxStatus;
import com.kaua.file.processor.infrastructure.services.eventbus.EventBus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PublishOutboxEventsJobTest extends UnitTest {

    @Mock
    private OutboxRepository outboxRepository;

    @Mock
    private EventBus eventBus;

    @InjectMocks
    private PublishOutboxEventsJob job;

    @Test
    void givenNoPendingOutboxEvents_whenPublishJobRuns_thenNothingIsPublished() {
        Mockito.when(outboxRepository.outboxOfStatusAndOccurredOn(OutboxStatus.PENDING))
                .thenReturn(List.of());

        job.publish();

        verify(outboxRepository, times(1))
                .outboxOfStatusAndOccurredOn(OutboxStatus.PENDING);
        verifyNoInteractions(eventBus);
    }

    @Test
    void givenPendingOutboxEvents_whenPublishJobRuns_thenEventsArePublished() {
        var outboxEvent = createOutboxEvent(OutboxStatus.PENDING);
        Mockito.when(outboxRepository.outboxOfStatusAndOccurredOn(OutboxStatus.PENDING))
                .thenReturn(List.of(outboxEvent));

        job.publish();

        verify(outboxRepository, times(1))
                .outboxOfStatusAndOccurredOn(OutboxStatus.PENDING);
        verify(eventBus, times(1))
                .publish(any());
        verify(outboxRepository, times(1))
                .markAsCompleted(outboxEvent);
    }

    @Test
    void givenPendingOutboxEventsThatFailToPublish_whenPublishJobRuns_thenEventsAreMarkedAsFailed() {
        var outboxEvent = createOutboxEvent(OutboxStatus.PENDING);
        Mockito.when(outboxRepository.outboxOfStatusAndOccurredOn(OutboxStatus.PENDING))
                .thenReturn(List.of(outboxEvent));
        doThrow(new RuntimeException("Publishing failed"))
                .when(eventBus).publish(any());

        job.publish();

        verify(outboxRepository, times(1))
                .outboxOfStatusAndOccurredOn(OutboxStatus.PENDING);
        verify(eventBus, times(1))
                .publish(any());
        verify(outboxRepository, times(1))
                .markAsFailed(outboxEvent);
    }

    private OutboxEntity createOutboxEvent(OutboxStatus outboxStatus) {
        final var aEvent = new FakeDomainEvent(
                IdentifierUtils.generateNewULID().toString(),
                1L
        );

        return new OutboxEntity(
                aEvent.eventId(),
                aEvent.aggregateId(),
                aEvent.eventType(),
                aEvent.aggregateVersion(),
                outboxStatus,
                Json.writeValueAsString(aEvent),
                aEvent.occurredOn(),
                aEvent.getClass().getCanonicalName()
        );
    }
}
