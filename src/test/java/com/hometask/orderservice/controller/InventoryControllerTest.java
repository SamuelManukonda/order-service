package com.hometask.orderservice.controller;

import com.hometask.orderservice.dto.ProductDTO;
import com.hometask.orderservice.service.InventoryServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryController
 */
class InventoryControllerTest {

    private InventoryServiceClient inventoryServiceClient;
    private InventoryController inventoryController;

    @BeforeEach
    void setUp() {
        inventoryServiceClient = mock(InventoryServiceClient.class);
        inventoryController = new InventoryController(inventoryServiceClient);
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
        RuntimeException failure = new RuntimeException("Service unavailable");

        // Act
        ResponseEntity<List<ProductDTO>> response = inventoryController.getStaticProductsFallback(failure);

        // Assert
        assertNotNull(response);
        assertEquals(503, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}