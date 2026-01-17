package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.domain.exceptions.InternalErrorException;
import com.kaua.file.processor.infrastructure.outbox.OutboxEntity;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class KafkaEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventBus.class);

    private final KafkaTemplate<String, byte[]> kafkaTemplate;
    private final String topic;

    public KafkaEventBus(
            final KafkaTemplate<String, byte[]> kafkaTemplate,
            final String topic
    ) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate);
        this.topic = Objects.requireNonNull(topic);
    }

    @Override
    public void publish(final OutboxEntity outbox) {
        log.info("Publishing event {} to topic {}", outbox.eventType(), topic);

        try {
            final var aRecord = new ProducerRecord<String, byte[]>(
                    topic,
                    null,
                    outbox.payload()
            );
            aRecord.headers()
                    .add("event_type", outbox.eventType().getBytes(StandardCharsets.UTF_8))
                    .add("payload_type", outbox.payloadType().name().getBytes(StandardCharsets.UTF_8))
                    .add("event_version", String.valueOf(outbox.version()).getBytes(StandardCharsets.UTF_8))
                    .add("event_id", outbox.eventId().getBytes(StandardCharsets.UTF_8));

            this.kafkaTemplate.send(aRecord).get(1, TimeUnit.MINUTES); // Blocked
        } catch (Exception ex) {
            log.error(
                    "Failed to publish event {} to topic {}",
                    outbox.eventType(),
                    topic,
                    ex
            );
            throw InternalErrorException.with(
                    "Failed to publish event %s to topic %s"
                            .formatted(outbox.eventType(), topic)
            );
        }
    }

}
