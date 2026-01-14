package com.kaua.file.processor.infrastructure.listeners;

import com.kaua.file.processor.AbstractEmbeddedKafkaTest;
import com.kaua.file.processor.application.importjob.process.ProcessImportJobUseCase;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import com.kaua.file.processor.domain.exceptions.NotFoundException;
import com.kaua.file.processor.domain.importjob.ImportJob;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import com.kaua.file.processor.infrastructure.processedEvents.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;

class KafkaEventListenerTest extends AbstractEmbeddedKafkaTest {

    @MockitoBean
    private ProcessImportJobUseCase processImportJobUseCase;

    @MockitoBean
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.consumers.import-job-process.topics.[0]}")
    private String importJobProcessTopic;

    @Test
    void shouldConsumeEventAndAck() throws Exception {
        final var event = new ImportJobCreatedEvent(
                UUID.randomUUID().toString(),
                1L
        );

        final var latch = new CountDownLatch(1);

        Mockito.when(processedEventRepository.existsById(event.eventId()))
                .thenReturn(false);

        Mockito.doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(processImportJobUseCase).execute(any());
        Mockito.doNothing().when(processedEventRepository).save(event.eventId());

        kafkaTemplate.send(
                importJobProcessTopic,
                Json.writeValueAsString(event)
        );

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        Mockito.verify(processedEventRepository, times(1))
                .existsById(event.eventId());
        Mockito.verify(processImportJobUseCase, times(1))
                .execute(argThat(cmd ->
                        cmd.importJobId().equals(event.aggregateId())
                ));
        Mockito.verify(processedEventRepository, times(1))
                .save(event.eventId());
    }

    @Test
    void shouldRetryWhenProcessingFails() throws Exception {
        final var event = new ImportJobCreatedEvent(
                UUID.randomUUID().toString(),
                1L
        );

        final var expectedAttempts = 2;
        final var latch = new CountDownLatch(expectedAttempts);

        Mockito.when(processedEventRepository.existsById(event.eventId()))
                .thenReturn(false);
        Mockito.doAnswer(invocation -> {
            latch.countDown();
            throw new RuntimeException("boom");
        }).when(processImportJobUseCase).execute(any());
        Mockito.doNothing().when(processedEventRepository).save(event.eventId());

        kafkaTemplate.send(
                importJobProcessTopic,
                Json.writeValueAsString(event)
        );

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        Mockito.verify(processedEventRepository, times(expectedAttempts))
                .existsById(event.eventId());
        Mockito.verify(processImportJobUseCase, times(2))
                .execute(argThat(cmd ->
                        cmd.importJobId().equals(event.aggregateId())
                ));
        Mockito.verify(processedEventRepository, times(0))
                .save(event.eventId());
    }

    @Test
    void shouldAckAndNotRetryWhenImportJobNotFound() throws Exception {
        final var event = new ImportJobCreatedEvent(
                UUID.randomUUID().toString(),
                1L
        );

        final var latch = new CountDownLatch(1);

        Mockito.when(processedEventRepository.existsById(event.eventId()))
                .thenReturn(false);
        Mockito.doAnswer(invocation -> {
            latch.countDown();
            throw NotFoundException.with(ImportJob.class, event.aggregateId()).get();
        }).when(processImportJobUseCase).execute(any());

        kafkaTemplate.send(
                importJobProcessTopic,
                Json.writeValueAsString(event)
        );

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        Mockito.verify(processedEventRepository, times(1))
                .existsById(event.eventId());
        Mockito.verify(processImportJobUseCase, times(1))
                .execute(argThat(cmd ->
                        cmd.importJobId().equals(event.aggregateId())
                ));
        Mockito.verify(processedEventRepository, times(0))
                .save(event.eventId());
    }

    @Test
    void shouldNotProcessEventIfAlreadyProcessed() throws Exception {
        final var event = new ImportJobCreatedEvent(
                UUID.randomUUID().toString(),
                1L
        );

        final var latch = new CountDownLatch(1);

        Mockito.doAnswer(invocation -> {
            latch.countDown();
            return true;
        }).when(processedEventRepository).existsById(event.eventId());

        kafkaTemplate.send(
                importJobProcessTopic,
                Json.writeValueAsString(event)
        );

        assertTrue(latch.await(10, TimeUnit.SECONDS));

        Mockito.verify(processedEventRepository, times(1))
                .existsById(event.eventId());
        Mockito.verify(processImportJobUseCase, times(0))
                .execute(any());
        Mockito.verify(processedEventRepository, times(0))
                .save(event.eventId());
    }
}
