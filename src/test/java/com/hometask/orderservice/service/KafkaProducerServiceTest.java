package com.hometask.orderservice.service;

import com.hometask.orderservice.dto.OrderPlaceRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KafkaProducerService
 */
@ExtendWith(MockitoExtension.class)
class KafkaProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaProducerService, "inventoryTopic", "update-inventory");
    }

    @Test
    void testSendMessage_Success() {
        // Arrange
        String topic = "test-topic";
        Object message = "test message";

        // Act
        kafkaProducerService.sendMessage(topic, message);

        // Assert
        verify(kafkaTemplate, times(1)).send(topic, message);
    }

    @Test
    void testSendMessage_WithComplexObject() {
        // Arrange
        String topic = "complex-topic";
        OrderPlaceRequest message = new OrderPlaceRequest("product123", 5);

        // Act
        kafkaProducerService.sendMessage(topic, message);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), messageCaptor.capture());

        assertEquals(topic, topicCaptor.getValue());
        assertEquals(message, messageCaptor.getValue());
    }

    @Test
    void testSendOrderPlacedMessage_Success() {
        // Arrange
        OrderPlaceRequest orderRequest = new OrderPlaceRequest("product456", 10);

        // Act
        kafkaProducerService.sendOrderPlacedMessage(orderRequest);

        // Assert
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> messageCaptor = ArgumentCaptor.forClass(Object.class);

        verify(kafkaTemplate, times(1)).send(topicCaptor.capture(), messageCaptor.capture());

        assertEquals("update-inventory", topicCaptor.getValue());
        assertEquals(orderRequest, messageCaptor.getValue());
    }

    @Test
    void testSendOrderPlacedMessage_VerifyCorrectTopic() {
        // Arrange
        OrderPlaceRequest orderRequest = new OrderPlaceRequest("product789", 3);

        // Act
        kafkaProducerService.sendOrderPlacedMessage(orderRequest);

        // Assert
        verify(kafkaTemplate, times(1)).send(eq("update-inventory"), eq(orderRequest));
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    void testSendOrderPlacedMessage_WithDifferentQuantities() {
        // Arrange
        OrderPlaceRequest orderRequest1 = new OrderPlaceRequest("product1", 1);
        OrderPlaceRequest orderRequest2 = new OrderPlaceRequest("product2", 100);

        // Act
        kafkaProducerService.sendOrderPlacedMessage(orderRequest1);
        kafkaProducerService.sendOrderPlacedMessage(orderRequest2);

        // Assert
        verify(kafkaTemplate, times(2)).send(eq("update-inventory"), any(OrderPlaceRequest.class));
    }
}

