package com.kaua.file.processor.infrastructure.configurations;

import com.kaua.file.processor.infrastructure.services.eventbus.ApplicationEventBus;
import com.kaua.file.processor.infrastructure.services.eventbus.EventBus;
import com.kaua.file.processor.infrastructure.services.eventbus.KafkaEventBus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration(proxyBeanMethods = false)
public class EventBusConfig {

    @Bean
    @ConditionalOnProperty(name = "application.eventbus", havingValue = "in-memory", matchIfMissing = true)
    public EventBus inMemoryEventBus(final ApplicationContext applicationContext) {
        return new ApplicationEventBus(applicationContext);
    }

    @Bean
    @ConditionalOnProperty(name = "application.eventbus", havingValue = "kafka")
    public EventBus kafkaEventBus(final KafkaTemplate<String, byte[]> kafkaTemplate, @Value("${kafka.topics.events}") final String topic) {
        return new KafkaEventBus(kafkaTemplate, topic);
    }
}
