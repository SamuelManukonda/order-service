//package com.hometask.orderservice.service;
//
//import com.hometask.orderservice.dto.OrderPlaceRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
///**
// * Unit tests for OrderService
// */
//@ExtendWith(MockitoExtension.class)
//class OrderServiceTest {
//
//    @Mock
//    private KafkaProducerService kafkaProducerService;
//
//    @InjectMocks
//    private OrderService orderService;
//
//    @BeforeEach
//    void setUp() {
//        ReflectionTestUtils.setField(orderService, "inventoryTopic", "update-inventory");
//    }
//
//    @Test
//    void testPlaceOrder_Success() {
//        // Arrange
//        String productId = "product123";
//        int quantity = 5;
//
//        // Act
//        String result = orderService.placeOrder(productId, quantity);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.startsWith("Order placed successfully!"));
//
//        // Verify that kafka message was sent
//        ArgumentCaptor<OrderPlaceRequest> captor = ArgumentCaptor.forClass(OrderPlaceRequest.class);
//        verify(kafkaProducerService, times(1)).updateInventoryMessage(captor.capture());
//
//        OrderPlaceRequest capturedRequest = captor.getValue();
//        assertEquals(productId, capturedRequest.productId());
//        assertEquals(quantity, capturedRequest.quantity());
//    }
//
//    @Test
//    void testPlaceOrder_WithDifferentProductId() {
//        // Arrange
//        String productId = "product456";
//        int quantity = 10;
//
//        // Act
//        String result = orderService.placeOrder(productId, quantity);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.contains("Order placed successfully!"));
//
//        // Verify that kafka message was sent with correct parameters
//        ArgumentCaptor<OrderPlaceRequest> captor = ArgumentCaptor.forClass(OrderPlaceRequest.class);
//        verify(kafkaProducerService, times(1)).updateInventoryMessage(captor.capture());
//
//        OrderPlaceRequest capturedRequest = captor.getValue();
//        assertEquals(productId, capturedRequest.productId());
//        assertEquals(quantity, capturedRequest.quantity());
//    }
//
//    @Test
//    void testPlaceOrder_WithMinimumQuantity() {
//        // Arrange
//        String productId = "product789";
//        int quantity = 1;
//
//        // Act
//        String result = orderService.placeOrder(productId, quantity);
//
//        // Assert
//        assertNotNull(result);
//        assertTrue(result.startsWith("Order placed successfully!"));
//        verify(kafkaProducerService, times(1)).updateInventoryMessage(any(OrderPlaceRequest.class));
//    }
//
//    @Test
//    void testPlaceOrder_VerifyKafkaInteraction() {
//        // Arrange
//        String productId = "testProduct";
//        int quantity = 3;
//
//        // Act
//        orderService.placeOrder(productId, quantity);
//
//        // Assert - verify that Kafka producer was called exactly once
//        verify(kafkaProducerService, times(1)).updateInventoryMessage(any(OrderPlaceRequest.class));
//    }
//}
//
