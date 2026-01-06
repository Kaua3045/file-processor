package com.kaua.file.processor.infrastructure.listeners;

import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.infrastructure.jobs.FakeDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class InMemoryEventListenerTest extends UnitTest {

    private InMemoryEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new InMemoryEventListener();
    }

    @Test
    void givenAValidImportJobCreatedEvent_whenHandleEvents_thenLogsInfo() {
        final var event = new ImportJobCreatedEvent(
                IdentifierUtils.generateNewULID().toString(),
                1L
        );

        listener.handleEvents(event);
    }

    @Test
    void givenAnUnrecognizedEvent_whenHandleEvents_thenThrowsIllegalArgumentException() {
        final var event = new FakeDomainEvent(
                IdentifierUtils.generateNewULID().toString(),
                1L
        );

        assertThrows(IllegalArgumentException.class, () -> listener.handleEvents(event));
    }
}
