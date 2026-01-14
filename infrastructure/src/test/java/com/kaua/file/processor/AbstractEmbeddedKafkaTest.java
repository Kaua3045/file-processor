package com.kaua.file.processor;

import com.kaua.file.processor.infrastructure.configurations.WebServerConfig;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.RecordsToDelete;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

//@EmbeddedKafka(partitions = 1, brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"})
@EmbeddedKafka(partitions = 1)
@ActiveProfiles("test-integration-kafka")
@SpringBootTest(
        classes = {WebServerConfig.class},
        properties = {"kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Tag("heavyIntegrationTest")
public abstract class AbstractEmbeddedKafkaTest {

    @Autowired
    protected EmbeddedKafkaBroker kafkaBroker;

    private Producer<String, String> producer;
    private AdminClient admin;

    @BeforeAll
    void init() {
        admin = AdminClient.create(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBroker.getBrokersAsString()));

        producer = new DefaultKafkaProducerFactory<>(KafkaTestUtils.producerProps(kafkaBroker), new StringSerializer(), new StringSerializer())
                .createProducer();
    }

    @AfterAll
    void shutdown() {
        producer.close();
    }

    protected AdminClient admin() {
        return admin;
    }

    protected Producer<String, String> producer() {
        return producer;
    }

    protected void cleanUpMessages(final String prefixTopicName) throws ExecutionException, InterruptedException, TimeoutException {
        final var aTopicsNames = admin().listTopics().listings().get(1, TimeUnit.MINUTES).stream()
                .map(TopicListing::name)
                .filter(it -> it.contains(prefixTopicName))
                .toList();

        final var aRecordsToDelete = aTopicsNames.stream()
                .collect(Collectors.toMap(name ->
                                new TopicPartition(name, 0),
                        name -> RecordsToDelete.beforeOffset(-1)));

        admin().deleteRecords(aRecordsToDelete).all().get(1, TimeUnit.MINUTES);
    }
}
