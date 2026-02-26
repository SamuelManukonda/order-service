package com.hometask.orderservice.service;

import com.hometask.orderservice.dto.OrderPlaceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
public class KafkaProducerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.inventory.update.topic}")
    private String inventoryTopic;

    @Value("${kafka.orders.topic}")
    private String orderPlacedTopic;


    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String topic, Object message) {
        logger.info("Sending message to topic {}: {}", topic, message);
        kafkaTemplate.send(topic, message);
    }

    public void updateInventoryMessage(OrderPlaceRequest orderPlaceRequest) {
        logger.info("Sending update inventory message': {}", orderPlaceRequest);
        kafkaTemplate.send(inventoryTopic, orderPlaceRequest);
    }
    public void sendOrderPlaceRequest(OrderPlaceRequest orderPlaceRequest) {
        logger.info("Sending order placed message to topic 'update-inventory': {}", orderPlaceRequest);
        kafkaTemplate.send(orderPlacedTopic, orderPlaceRequest);
    }
}
