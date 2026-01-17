package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import com.kaua.file.processor.infrastructure.jobs.FakeDomainEvent;
import com.kaua.file.processor.infrastructure.outbox.OutboxEntity;
import com.kaua.file.processor.infrastructure.outbox.OutboxPayloadType;
import com.kaua.file.processor.infrastructure.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationContext;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ApplicationEventBusTest extends UnitTest {

    @Mock
    private ApplicationContext applicationContext;

    private ApplicationEventBus eventBus;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        eventBus = new ApplicationEventBus(applicationContext);
    }

    @Test
    void givenOutboxMessage_whenPublish_thenDelegatesToApplicationContext() {
        final var event = new FakeDomainEvent(IdentifierUtils.generateNewULID().toString(), 0L);

        final var aOutboxEntity = new OutboxEntity(
                event.eventId(),
                event.aggregateId(),
                event.eventType(),
                event.aggregateVersion(),
                OutboxStatus.COMPLETED,
                Json.writeValueAsBytes(event),
                event.occurredOn(),
                OutboxPayloadType.JSON
        );

        eventBus.publish(aOutboxEntity);

        verify(applicationContext, times(1)).publishEvent(aOutboxEntity);
    }
}