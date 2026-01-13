package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.domain.UnitTest;
import com.kaua.file.processor.domain.events.DomainEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEventBusTest extends UnitTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private DomainEvent domainEvent;

    private KafkaEventBus eventBus;

    @BeforeEach
    void setup() {
        eventBus = new KafkaEventBus(kafkaTemplate, "test-topic");
    }

    @Test
    void shouldPublishEventToKafka() {
        final var future = new CompletableFuture<SendResult<String, Object>>();
        when(kafkaTemplate.send("test-topic", domainEvent))
                .thenReturn(future);

        eventBus.publish(domainEvent);

        verify(kafkaTemplate).send("test-topic", domainEvent);
    }

    @Test
    void shouldHandleSuccessfulPublish() {
        var future = new CompletableFuture<SendResult<String, Object>>();
        when(kafkaTemplate.send(anyString(), any()))
                .thenReturn(future);

        eventBus.publish(domainEvent);

        var metadata = mock(RecordMetadata.class);
        when(metadata.partition()).thenReturn(1);
        when(metadata.offset()).thenReturn(42L);

        var sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(metadata);

        future.complete(sendResult);

        verify(kafkaTemplate).send(anyString(), eq(domainEvent));
    }

    @Test
    void shouldHandlePublishError() {
        var future = new CompletableFuture<SendResult<String, Object>>();
        when(kafkaTemplate.send(anyString(), any()))
                .thenReturn(future);

        eventBus.publish(domainEvent);

        future.completeExceptionally(new RuntimeException("Kafka down"));

        verify(kafkaTemplate).send(anyString(), eq(domainEvent));
    }
}
