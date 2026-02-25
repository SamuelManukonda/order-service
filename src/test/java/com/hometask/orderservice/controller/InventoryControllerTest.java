package com.hometask.orderservice.controller;

import com.hometask.orderservice.dto.ProductDTO;
import com.hometask.orderservice.service.InventoryServiceClient;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryController
 */
class InventoryControllerTest {

    private InventoryServiceClient inventoryServiceClient;
    private InventoryController inventoryController;
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        inventoryServiceClient = mock(InventoryServiceClient.class);
        inventoryController = new InventoryController(inventoryServiceClient);
        circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();

    }

    @Test
    void testGetAllProducts_Success() {
        // Arrange
        List<ProductDTO> mockProducts = Arrays.asList(
                new ProductDTO("1", "Product 1", "Description 1", BigDecimal.valueOf(99.99), "USD", "Electronics", 10, "http://example.com/image1.jpg", BigDecimal.valueOf(4.5)),
                new ProductDTO("2", "Product 2", "Description 2", BigDecimal.valueOf(49.99), "Rupee", "Books", 5, "http://example.com/image2.jpg", BigDecimal.valueOf(4.0))
        );
        when(inventoryServiceClient.getAllProducts()).thenReturn(mockProducts);

        // Act
        ResponseEntity<List<ProductDTO>> response = inventoryController.getAllProducts();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void testGetAllProducts_Fallback() {
        // Arrange
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("inventoryService");
        circuitBreaker.transitionToOpenState();

        when(inventoryServiceClient.getAllProducts()).thenThrow(new RuntimeException("Service unavailable"));

        // Act
        ResponseEntity<List<ProductDTO>> response = inventoryController.getAllProducts();

        // Assert
        assertNotNull(response);
        assertEquals(503, response.getStatusCode().value());
        assertNull(response.getBody());
    }


    @Test
    void testGetAllProductsAsync_Success() {
        // Arrange
        List<ProductDTO> mockProducts = Collections.singletonList(
                new ProductDTO("1", "Product 1", "Description 1", BigDecimal.valueOf(99.99), "USD", "Electronics", 10, "http://example.com/image1.jpg", BigDecimal.valueOf(4.5))
        );
        when(inventoryServiceClient.getAllProductsAsync()).thenReturn(reactor.core.publisher.Mono.just(mockProducts));

        // Act
        ResponseEntity<List<ProductDTO>> response = inventoryController.getAllProductsAsync().block();

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetAllProductsAsync_Error() {
        // Arrange
        when(inventoryServiceClient.getAllProductsAsync()).thenReturn(reactor.core.publisher.Mono.error(new RuntimeException("Service unavailable")));

        // Act
        ResponseEntity<List<ProductDTO>> response = inventoryController.getAllProductsAsync().block();

        // Assert
        assertNotNull(response);
        assertEquals(500, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}
