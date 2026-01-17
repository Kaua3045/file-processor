package com.kaua.file.processor.infrastructure.services.eventbus;

import com.kaua.file.processor.infrastructure.outbox.OutboxEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

public class ApplicationEventBus implements EventBus {

    private static final Logger log = LoggerFactory.getLogger(ApplicationEventBus.class);

    private final ApplicationContext applicationContext;

    public ApplicationEventBus(final ApplicationContext applicationContext) {
        this.applicationContext = Objects.requireNonNull(applicationContext);
    }

    @Override
    public void publish(final OutboxEntity outbox) {
        log.info("Publishing event to the application context: {}", outbox);
        this.applicationContext.publishEvent(outbox);
        log.info("Event published to the application context: {}", outbox);
    }
}
