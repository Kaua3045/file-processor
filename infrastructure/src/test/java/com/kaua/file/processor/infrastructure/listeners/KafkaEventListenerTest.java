package com.kaua.file.processor.infrastructure.listeners;

import com.kaua.file.processor.AbstractEmbeddedKafkaTest;
import com.kaua.file.processor.application.importjob.process.ProcessImportJobCommand;
import com.kaua.file.processor.application.importjob.process.ProcessImportJobUseCase;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import com.kaua.file.processor.domain.exceptions.NotFoundException;
import com.kaua.file.processor.domain.importjob.ImportJob;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KafkaEventListenerTest extends AbstractEmbeddedKafkaTest {

    @MockitoBean
    private ProcessImportJobUseCase processImportJobUseCase;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.consumers.import-job-process.topics.[0]}")
    private String importJobProcessTopic;

    @BeforeEach
    void setup() throws ExecutionException, InterruptedException, TimeoutException {
        cleanUpMessages(importJobProcessTopic);
    }

    @Test
    void shouldConsumeEventAndAck() throws Exception {
        final var event = new ImportJobCreatedEvent(
                UUID.randomUUID().toString(),
                1L
        );

        final var payload = Json.writeValueAsString(event);

        kafkaTemplate.send(importJobProcessTopic, payload);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        verify(processImportJobUseCase)
                                .execute(ProcessImportJobCommand.with(event.aggregateId()))
                );
    }

    @Test
    void shouldNotAckWhenProcessingFails() {
        final var event = new ImportJobCreatedEvent(
                UUID.randomUUID().toString(),
                1L
        );

        final var payload = Json.writeValueAsString(event);

        doThrow(new RuntimeException("boom"))
                .when(processImportJobUseCase)
                .execute(any());

        kafkaTemplate.send(importJobProcessTopic, payload);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        verify(processImportJobUseCase, atLeast(2))
                                .execute(any())
                );
    }

    @Test
    void shouldNotFoundImportJobAndAck() throws Exception {
        final var event = new ImportJobCreatedEvent(
                UUID.randomUUID().toString(),
                1L
        );

        final var payload = Json.writeValueAsString(event);

        doThrow(NotFoundException.with(ImportJob.class, event.aggregateId()).get())
                .when(processImportJobUseCase)
                .execute(any());

        kafkaTemplate.send(importJobProcessTopic, payload);

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() ->
                        verify(processImportJobUseCase)
                                .execute(ProcessImportJobCommand.with(event.aggregateId()))
                );
    }
}
