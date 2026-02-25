package com.hometask.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

//    @KafkaListener(topics = "example-topic", groupId = "orders-group")
//    public void consumeMessage(Object message) {
//        logger.info("Consumed message: {}", message);
//    }
}
