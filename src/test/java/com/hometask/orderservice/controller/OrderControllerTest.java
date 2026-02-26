package com.hometask.orderservice.controller;

import com.hometask.orderservice.dto.ProductDTO;
import com.hometask.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderController
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Order Controller Unit Tests")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @Mock
    private InventoryController inventoryController;

    @InjectMocks
    private OrderController orderController;

    private List<ProductDTO> mockProducts;

    @BeforeEach
    void setUp() {
        mockProducts = Arrays.asList(
                new ProductDTO("prod1", "Laptop", "High-end Laptop", BigDecimal.valueOf(999.99), "USD", "Electronics", 50, "http://example.com/laptop.jpg", BigDecimal.valueOf(4.8)),
                new ProductDTO("prod2", "Mouse", "Wireless Mouse", BigDecimal.valueOf(29.99), "USD", "Accessories", 200, "http://example.com/mouse.jpg", BigDecimal.valueOf(4.5))
        );
    }

    @Test
    @DisplayName("Place order successfully with sufficient stock")
    void testPlaceOrder_Success() {
        // Arrange
        when(inventoryController.getAllProducts()).thenReturn(ResponseEntity.ok(mockProducts));
        when(orderService.placeOrder("prod1", 5)).thenReturn("Order placed successfully! " + Math.random() * 1000);

        // Act
        ResponseEntity<String> response = orderController.placeOrder("prod1", 5);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().contains("Order placed successfully!"));
        verify(orderService, times(1)).placeOrder("prod1", 5);
        verify(inventoryController, times(1)).getAllProducts();
    }

    @Test
    @DisplayName("Order placement fails with insufficient stock")
    void testPlaceOrder_InsufficientStock() {
        // Arrange
        when(inventoryController.getAllProducts()).thenReturn(ResponseEntity.ok(mockProducts));

        // Act
        ResponseEntity<String> response = orderController.placeOrder("prod1", 100);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Insufficient stock"));
        verify(orderService, never()).placeOrder(anyString(), anyInt());
    }

    @Test
    @DisplayName("Order placement fails when product not found")
    void testPlaceOrder_ProductNotFound() {
        // Arrange
        when(inventoryController.getAllProducts()).thenReturn(ResponseEntity.ok(mockProducts));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderController.placeOrder("nonexistent", 5));
        verify(orderService, never()).placeOrder(anyString(), anyInt());
    }

    @Test
    @DisplayName("Place order with minimum quantity (1)")
    void testPlaceOrder_MinimumQuantity() {
        // Arrange
        when(inventoryController.getAllProducts()).thenReturn(ResponseEntity.ok(mockProducts));
        when(orderService.placeOrder("prod2", 1)).thenReturn("Order placed successfully! 123");

        // Act
        ResponseEntity<String> response = orderController.placeOrder("prod2", 1);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).placeOrder("prod2", 1);
    }

    @Test
    @DisplayName("Place order with maximum available stock")
    void testPlaceOrder_MaximumStock() {
        // Arrange
        when(inventoryController.getAllProducts()).thenReturn(ResponseEntity.ok(mockProducts));
        when(orderService.placeOrder("prod2", 200)).thenReturn("Order placed successfully! 456");

        // Act
        ResponseEntity<String> response = orderController.placeOrder("prod2", 200);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderService, times(1)).placeOrder("prod2", 200);
    }

    @Test
    @DisplayName("Place order with quantity exceeding stock by 1")
    void testPlaceOrder_StockExceededByOne() {
        // Arrange
        when(inventoryController.getAllProducts()).thenReturn(ResponseEntity.ok(mockProducts));

        // Act
        ResponseEntity<String> response = orderController.placeOrder("prod2", 201);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("Insufficient stock"));
        verify(orderService, never()).placeOrder(anyString(), anyInt());
    }

    @Test
    @DisplayName("Place multiple orders sequentially")
    void testPlaceMultipleOrders() {
        // Arrange
        when(inventoryController.getAllProducts()).thenReturn(ResponseEntity.ok(mockProducts));
        when(orderService.placeOrder(anyString(), anyInt())).thenReturn("Order placed successfully! 789");

        // Act
        ResponseEntity<String> response1 = orderController.placeOrder("prod1", 10);
        ResponseEntity<String> response2 = orderController.placeOrder("prod2", 20);
        ResponseEntity<String> response3 = orderController.placeOrder("prod1", 15);

        // Assert
        assertEquals(HttpStatus.OK, response1.getStatusCode());
        assertEquals(HttpStatus.OK, response2.getStatusCode());
        assertEquals(HttpStatus.OK, response3.getStatusCode());
        verify(orderService, times(3)).placeOrder(anyString(), anyInt());
    }

    @Test
    @DisplayName("Order placement handles empty product list")
    void testPlaceOrder_EmptyProductList() {
        // Arrange
        when(inventoryController.getAllProducts()).thenReturn(ResponseEntity.ok(List.of()));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> orderController.placeOrder("prod1", 5));
        verify(orderService, never()).placeOrder(anyString(), anyInt());
    }

    @Test
    @DisplayName("Order placement with zero quantity should fail at service level")
    void testPlaceOrder_ZeroQuantity() {
        // Act - Should fail because stock < quantity
        ResponseEntity<String> response = orderController.placeOrder("prod1", 0);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verify(orderService, never()).placeOrder(anyString(), anyInt());
    }
}

