package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.exceptions.InternalErrorException;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.infrastructure.jobs.FakeDomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventBusTest extends UnitTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    private KafkaEventBus eventBus;

    @BeforeEach
    void setup() {
        eventBus = new KafkaEventBus(kafkaTemplate, "test-topic");
    }

    @Test
    void shouldHandleSuccessfulPublish() {
        var sendResult = mock(SendResult.class);
        when(kafkaTemplate.send(anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        eventBus.publish(new FakeDomainEvent(IdentifierUtils.generateNewULID().toString(), 0L));

        verify(kafkaTemplate).send(anyString(), any());
    }

    @Test
    void shouldHandlePublishError() {
        when(kafkaTemplate.send(anyString(), any()))
                .thenThrow(new RuntimeException("Kafka down"));

        assertThrows(
                InternalErrorException.class,
                () -> eventBus.publish(new FakeDomainEvent(IdentifierUtils.generateNewULID().toString(), 0L))
        );
    }

}
