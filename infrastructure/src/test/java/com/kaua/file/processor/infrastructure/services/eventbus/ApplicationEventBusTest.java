package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.infrastructure.jobs.FakeDomainEvent;
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
        final var message = new FakeDomainEvent(IdentifierUtils.generateNewULID().toString(), 1l);

        eventBus.publish(message);

        verify(applicationContext, times(1)).publishEvent(message);
    }
}