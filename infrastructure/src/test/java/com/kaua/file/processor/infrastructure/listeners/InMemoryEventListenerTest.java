package com.kaua.file.processor.infrastructure.listeners;

import com.kaua.file.processor.application.importjob.process.ProcessImportJobUseCase;
import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import com.kaua.file.processor.infrastructure.jobs.FakeDomainEvent;
import com.kaua.file.processor.infrastructure.outbox.OutboxEntity;
import com.kaua.file.processor.infrastructure.outbox.OutboxPayloadType;
import com.kaua.file.processor.infrastructure.outbox.OutboxStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InMemoryEventListenerTest extends UnitTest {

    private InMemoryEventListener listener;

    @Mock
    private ProcessImportJobUseCase processImportJobUseCase;

    @BeforeEach
    void setUp() {
        listener = new InMemoryEventListener(processImportJobUseCase);
    }

    @Test
    void givenAValidImportJobCreatedEvent_whenHandleEvents_thenLogsInfo() {
        final var event = new ImportJobCreatedEvent(
                IdentifierUtils.generateNewULID().toString(),
                1L
        );
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

        listener.handleEvents(aOutboxEntity);

        verify(processImportJobUseCase, times(1))
                .execute(any());
    }

    @Test
    void givenAnUnrecognizedEvent_whenHandleEvents_thenThrowsIllegalArgumentException() {
        final var event = new FakeDomainEvent(
                IdentifierUtils.generateNewULID().toString(),
                1L
        );

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

        assertThrows(IllegalArgumentException.class, () -> listener.handleEvents(aOutboxEntity));
    }
}
