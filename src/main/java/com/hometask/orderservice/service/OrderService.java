package com.hometask.orderservice.service;

import com.hometask.orderservice.dto.OrderPlaceRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderService {


    private final KafkaProducerService kafkaProducerService;

    @Value("${kafka.inventory.update.topic}")
    private String inventoryTopic;


    public OrderService(KafkaProducerService kafkaProducerService) {
        this.kafkaProducerService = kafkaProducerService;
    }

    public String placeOrder(String productId, int quantity) {
        updateInventory(productId, quantity);
        double random = Math.random() * 1000;
        return "Order placed successfully! " + random;
    }

    private void updateInventory(String productId, int quantity) {
        OrderPlaceRequest orderPlaceRequest = new OrderPlaceRequest(productId, quantity);
        kafkaProducerService.sendOrderPlacedMessage(orderPlaceRequest);
    }
}
