package com.kaua.file.processor.infrastructure.configurations;

import com.kaua.file.processor.infrastructure.configurations.properties.kafka.KafkaProperties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Bean
    public ProducerFactory<String, byte[]> producerFactory(final KafkaProperties kafkaProperties) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
        configProps.put(ProducerConfig.ACKS_CONFIG, "all");
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // todo check if this is ok
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5); // todo check if this is ok

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, byte[]> kafkaTemplate(final KafkaProperties kafkaProperties) {
        return new KafkaTemplate<>(producerFactory(kafkaProperties));
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, byte[]>> kafkaListenerFactory(final KafkaProperties kafkaProperties) {
        final var factory = new ConcurrentKafkaListenerContainerFactory<String, byte[]>();
        factory.setConsumerFactory(consumerFactory(kafkaProperties));
        factory.getContainerProperties().setPollTimeout(kafkaProperties.getPoolTimeout());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        return factory;
    }

    private ConsumerFactory<String, byte[]> consumerFactory(final KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(kafkaProperties));
    }

    private Map<String, Object> consumerConfigs(final KafkaProperties kafkaProperties) {
        final var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
        props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, kafkaProperties.isAutoCreateTopics());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaProperties.isAutoCommit());
        return props;
    }
}
