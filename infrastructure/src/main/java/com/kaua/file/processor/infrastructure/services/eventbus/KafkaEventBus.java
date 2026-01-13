package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.domain.events.DomainEvent;
import com.kaua.file.processor.domain.exceptions.InternalErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Objects;

public class KafkaEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventBus.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String topic;

    public KafkaEventBus(
            final KafkaTemplate<String, Object> kafkaTemplate,
            final String topic
    ) {
        this.kafkaTemplate = Objects.requireNonNull(kafkaTemplate);
        this.topic = Objects.requireNonNull(topic);
    }

    @Override
    public void publish(final DomainEvent event) {
        log.info("Publishing event {} to topic {}", event.eventType(), topic);

        try {
            kafkaTemplate.send(topic, event).get(); // block to ACK
        } catch (Exception ex) {
            log.error(
                    "Failed to publish event {} to topic {}",
                    event.eventType(),
                    topic,
                    ex
            );
            throw InternalErrorException.with(
                    "Failed to publish event %s to topic %s"
                            .formatted(event.eventType(), topic)
            );
        }
    }

}
