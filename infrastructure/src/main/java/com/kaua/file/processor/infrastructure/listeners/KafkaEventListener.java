package com.kaua.file.processor.infrastructure.listeners;

import com.kaua.file.processor.application.importjob.process.ProcessImportJobCommand;
import com.kaua.file.processor.application.importjob.process.ProcessImportJobUseCase;
import com.kaua.file.processor.application.wrapper.Metrics;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import com.kaua.file.processor.domain.exceptions.NotFoundException;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
import com.kaua.file.processor.infrastructure.processedEvents.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class KafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventListener.class);

    private final ProcessImportJobUseCase processImportJobUseCase;
    private final ProcessedEventRepository processedEventRepository;
    private final Metrics metrics;

    public KafkaEventListener(
            final ProcessImportJobUseCase processImportJobUseCase,
            final ProcessedEventRepository processedEventRepository,
            final Metrics metrics
    ) {
        this.processImportJobUseCase = Objects.requireNonNull(processImportJobUseCase);
        this.processedEventRepository = Objects.requireNonNull(processedEventRepository);
        this.metrics = Objects.requireNonNull(metrics);
    }

    @KafkaListener(
            concurrency = "${kafka.consumers.import-job-process.concurrency}",
            containerFactory = "kafkaListenerFactory",
            topics = {
                    "${kafka.consumers.import-job-process.topics.[0]}",
            },
            groupId = "${kafka.consumers.import-job-process.group-id}",
            // generate a random id for the consumer
            properties = {
                    "auto.offset.reset=${kafka.consumers.import-job-process.auto-offset-reset}"
            }
    )
    public void onMessage(@Payload final String message, Acknowledgment ack) {
        log.info("Received message: {}", message);
        final var aStart = System.currentTimeMillis();
        this.metrics.incrementCounter("kafka_event_received", 1);

        // TODO in future handle different types of events
        final var aEvent = Json.readValue(message, ImportJobCreatedEvent.class);

        if (this.processedEventRepository.existsById(aEvent.eventId())) {
            log.warn("Event with id: {} has already been processed. Acknowledging message to avoid reprocessing.", aEvent.eventId());
            this.metrics.incrementCounter("kafka_event_duplicated", 1);
            ack.acknowledge();
            this.metrics.incrementCounter("kafka_event_acked", 1);
            return;
        }

        try {
            log.info("Processing import job with id: {}", aEvent.aggregateId());
            this.processImportJobUseCase.execute(ProcessImportJobCommand.with(
                    aEvent.aggregateId()
            ));
            this.processedEventRepository.save(aEvent.eventId());
            ack.acknowledge();

            this.metrics.incrementCounter("kafka_event_processed", 1);
            this.metrics.incrementCounter("kafka_event_acked", 1);

            log.info("Import job with id: {} processed successfully", aEvent.aggregateId());
        } catch (NotFoundException e) {
            log.warn("Import job with id: {} not found. Acknowledging message to avoid reprocessing.", aEvent.aggregateId());

            this.metrics.incrementCounter("kafka_event_not_found", 1);

            ack.acknowledge();

            this.metrics.incrementCounter("kafka_event_acked", 1);
        } catch (Exception ex) {
            log.error("Error processing import job with id: {}", aEvent.aggregateId(), ex);
            this.metrics.incrementCounter("kafka_event_failed", 1);
            throw ex;
        } finally {
            this.metrics.recordTime("kafka_processing_time_ms", System.currentTimeMillis() - aStart);
        }
    }
}
