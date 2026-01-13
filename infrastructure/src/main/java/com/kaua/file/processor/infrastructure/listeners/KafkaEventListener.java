package com.kaua.file.processor.infrastructure.listeners;

import com.kaua.file.processor.application.importjob.process.ProcessImportJobCommand;
import com.kaua.file.processor.application.importjob.process.ProcessImportJobUseCase;
import com.kaua.file.processor.domain.events.ImportJobCreatedEvent;
import com.kaua.file.processor.infrastructure.configurations.json.Json;
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

    public KafkaEventListener(final ProcessImportJobUseCase processImportJobUseCase) {
        this.processImportJobUseCase = Objects.requireNonNull(processImportJobUseCase);
    }

    @KafkaListener(
            concurrency = "${kafka.consumers.import-job-process.concurrency}",
            containerFactory = "kafkaListenerFactory",
            topics = {
                    "${kafka.consumers.import-job-process.topics.[0]}",
            },
            groupId = "${kafka.consumers.import-job-process.group-id}",
            // generate a random id for the consumer
            id = "${kafka.consumers.import-job-process.id}-#{T(java.util.UUID).randomUUID().toString()}",
            properties = {
                    "auto.offset.reset=${kafka.consumers.import-job-process.auto-offset-reset}"
            }
    )
    public void onMessage(@Payload final String message, Acknowledgment ack) {
        log.info("Received message: {}", message);
        // TODO in future handle different types of events
        final var aEvent = Json.readValue(message, ImportJobCreatedEvent.class);

        // TODO check if the event has already been processed

        try {
            log.info("Processing import job with id: {}", aEvent.aggregateId());
            this.processImportJobUseCase.execute(ProcessImportJobCommand.with(
                    aEvent.aggregateId()
            ));
            ack.acknowledge();
            log.info("Import job with id: {} processed successfully", aEvent.aggregateId());
        } catch (Exception ex) {
            log.error("Error processing import job with id: {}", aEvent.aggregateId(), ex);
            throw ex;
        }
    }
}
