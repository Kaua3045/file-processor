package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.exceptions.InternalErrorException;
import com.kaua.file.processor.domain.utils.IdentifierUtils;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import com.kaua.file.processor.infrastructure.jobs.FakeDomainEvent;
import com.kaua.file.processor.infrastructure.outbox.OutboxEntity;
import com.kaua.file.processor.infrastructure.outbox.OutboxPayloadType;
import com.kaua.file.processor.infrastructure.outbox.OutboxStatus;
import org.apache.kafka.clients.producer.ProducerRecord;
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
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    private KafkaEventBus eventBus;

    @BeforeEach
    void setup() {
        eventBus = new KafkaEventBus(kafkaTemplate, "test-topic");
    }

    @Test
    void shouldHandleSuccessfulPublish() {
        var sendResult = mock(SendResult.class);
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

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

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    @Test
    void shouldHandlePublishError() {
        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenThrow(new RuntimeException("Kafka down"));

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

        assertThrows(
                InternalErrorException.class,
                () -> eventBus.publish(aOutboxEntity)
        );
    }

}
