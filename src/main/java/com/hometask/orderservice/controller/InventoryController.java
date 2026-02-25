package com.hometask.orderservice.controller;

import com.hometask.orderservice.dto.ProductDTO;
import com.hometask.orderservice.service.InventoryServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * REST Controller for inventory-related operations
 * Provides endpoints to interact with the Inventory Service
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    private final InventoryServiceClient inventoryServiceClient;

    public InventoryController(InventoryServiceClient inventoryServiceClient) {
        this.inventoryServiceClient = inventoryServiceClient;
        logger.info("InventoryController initialized");
    }

    /**
     * Endpoint to fetch all products from inventory service (synchronous)
     *
     * @return ResponseEntity containing list of products
     */
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "getStaticProductsFallback")
    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        logger.info("Received request to fetch all products");

        List<ProductDTO> products = inventoryServiceClient.getAllProducts();
        logger.info("Returning {} products to client", products.size());
        return ResponseEntity.ok(products);
    }


    public ResponseEntity<List<ProductDTO>> getStaticProductsFallback(Throwable throwable) {
        logger.warn("Inventory service unavailable, returning fallback response", throwable);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
