package com.hometask.orderservice.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KafkaConfig
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Kafka Configuration Tests")
class KafkaConfigTest {

    @Autowired
    private KafkaConfig kafkaConfig;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    @DisplayName("Kafka configuration bean is created")
    void testKafkaConfigBeanCreation() {
        assertNotNull(kafkaConfig);
    }

    @Test
    @DisplayName("KafkaTemplate bean is available")
    void testKafkaTemplateBeanAvailable() {
        assertNotNull(kafkaTemplate);
    }

    @Test
    @DisplayName("Kafka producer factory is configured")
    void testKafkaProducerFactory() {
        // Arrange & Act
        var producerFactory = kafkaConfig.producerFactory();

        // Assert
        assertNotNull(producerFactory);
    }

    @Test
    @DisplayName("Kafka consumer factory is configured")
    void testKafkaConsumerFactory() {
        // Arrange & Act
        var consumerFactory = kafkaConfig.consumerFactory();

        // Assert
        assertNotNull(consumerFactory);
    }

    @Test
    @DisplayName("Kafka listener container factory is configured")
    void testKafkaListenerContainerFactory() {
        // Arrange & Act
        var listenerContainerFactory = kafkaConfig.kafkaListenerContainerFactory();

        // Assert
        assertNotNull(listenerContainerFactory);
    }
}

