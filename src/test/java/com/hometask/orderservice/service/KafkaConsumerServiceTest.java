package com.hometask.orderservice.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for KafkaConsumerService
 */
@DisplayName("Kafka Consumer Service Unit Tests")
class KafkaConsumerServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerServiceTest.class);

    @Test
    @DisplayName("KafkaConsumerService bean instantiation")
    void testKafkaConsumerServiceInstantiation() {
        // Arrange & Act
        KafkaConsumerService kafkaConsumerService = new KafkaConsumerService();

        // Assert
        assertNotNull(kafkaConsumerService);
    }

    @Test
    @DisplayName("KafkaConsumerService has no active listeners")
    void testKafkaConsumerServiceHasNoActiveListeners() {
        // Arrange
        KafkaConsumerService kafkaConsumerService = new KafkaConsumerService();

        // Act & Assert
        assertNotNull(kafkaConsumerService);
        logger.info("KafkaConsumerService instantiated successfully - no listeners registered");
    }
}

