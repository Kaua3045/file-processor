package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.domain.events.DomainEvent;
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
        log.info("Publishing event {} to topic {}", event.getClass().getSimpleName(), topic);
        final var future = kafkaTemplate.send(topic, event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish event {} to topic {}", event.getClass().getSimpleName(), event.eventType(), ex);
            } else {
                log.info("Event {} published to topic {} partition {} with offset {}",
                        event.getClass().getSimpleName(),
                        event.eventType(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}
