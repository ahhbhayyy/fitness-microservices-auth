package com.fitness.activityservice.config;

import com.fitness.activityservice.model.Activity;
import com.fitness.activityservice.model.ActivityType;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = "activity-events")
@SpringJUnitConfig(KafkaConfig.class)
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "kafka.topic.name=activity-events",
        "kafka.topic.partitions=1",
        "kafka.topic.replicas=1"
})
class KafkaProducerIntegrationTests {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private KafkaTemplate<String, Activity> kafkaTemplate;

    @Test
    void publishesActivityEventsAsJsonWithoutProducerClassHeaders() throws Exception {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("activity-producer-test", "true", embeddedKafka);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<String, String>(consumerProps).createConsumer()) {
            embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "activity-events");

            Activity activity = Activity.builder()
                    .id("activity-1")
                    .userId("user-1")
                    .type(ActivityType.RUNNING)
                    .duration(30)
                    .caloriesBurned(250)
                    .startTime(LocalDateTime.of(2026, 5, 30, 9, 0))
                    .additionalMetrics(Map.of("distanceKm", 5))
                    .build();

            kafkaTemplate.send("activity-events", activity.getUserId(), activity).get(10, TimeUnit.SECONDS);

            ConsumerRecord<String, String> record = KafkaTestUtils.getSingleRecord(
                    consumer,
                    "activity-events",
                    Duration.ofSeconds(10)
            );

            assertThat(record.key()).isEqualTo("user-1");
            assertThat(record.value()).contains("\"id\":\"activity-1\"");
            assertThat(record.value()).contains("\"userId\":\"user-1\"");
            assertThat(record.value()).contains("\"type\":\"RUNNING\"");
            assertThat(record.headers().lastHeader("__TypeId__")).isNull();
        }
    }
}
