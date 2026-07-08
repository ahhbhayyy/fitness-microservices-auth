package com.fitness.aiservice.service;

import com.fitness.aiservice.config.KafkaConsumerConfig;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.ActivityType;
import com.fitness.aiservice.model.Recommendation;
import com.fitness.aiservice.repository.RecommendationRepository;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DirtiesContext
@EmbeddedKafka(partitions = 1, topics = "activity-events")
@SpringJUnitConfig(classes = {
        KafkaConsumerConfig.class,
        ActivityMessageListener.class,
        ActivityMessageListenerIntegrationTests.TestBeans.class
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=activity-processor-group",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "kafka.topic.name=activity-events",
        "kafka.topic.partitions=1",
        "kafka.topic.replicas=1"
})
class ActivityMessageListenerIntegrationTests {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    private ActivityAIService activityAIService;

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Test
    void consumesActivityEventsAndTriggersRecommendationCreation() throws Exception {
        Map<String, Object> producerProps = KafkaTestUtils.producerProps(embeddedKafka);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        producerProps.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        DefaultKafkaProducerFactory<String, Activity> producerFactory = new DefaultKafkaProducerFactory<>(producerProps);
        try {
            KafkaTemplate<String, Activity> kafkaTemplate = new KafkaTemplate<>(producerFactory);
            Activity activity = Activity.builder()
                    .id("activity-1")
                    .userId("user-1")
                    .type(ActivityType.CYCLING)
                    .duration(45)
                    .caloriesBurned(400)
                    .startTime(LocalDateTime.of(2026, 5, 30, 10, 0))
                    .additionalMetrics(Map.of("distanceKm", 18))
                    .build();
            Recommendation recommendation = Recommendation.builder()
                    .activityId("activity-1")
                    .userId("user-1")
                    .type("CYCLING")
                    .build();
            when(activityAIService.generateRecommendation(any(Activity.class))).thenReturn(recommendation);

            kafkaTemplate.send("activity-events", activity.getUserId(), activity).get(10, TimeUnit.SECONDS);

            verify(activityAIService, timeout(10000)).generateRecommendation(any(Activity.class));
            verify(recommendationRepository, timeout(10000)).save(recommendation);
        } finally {
            producerFactory.destroy();
        }
    }

    @TestConfiguration
    static class TestBeans {

        @Bean
        ActivityAIService activityAIService() {
            return mock(ActivityAIService.class);
        }

        @Bean
        RecommendationRepository recommendationRepository() {
            return mock(RecommendationRepository.class);
        }
    }
}
