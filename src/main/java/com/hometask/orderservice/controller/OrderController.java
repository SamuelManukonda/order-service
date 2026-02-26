package com.hometask.orderservice.controller;

import com.hometask.orderservice.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final InventoryController inventoryController;

    public OrderController(OrderService orderService, InventoryController inventoryController) {
        this.orderService = orderService;
        this.inventoryController = inventoryController;
    }

  @PostMapping("/place/{productId}/{quantity}")
  public ResponseEntity<String> placeOrder(@PathVariable String productId, @PathVariable int quantity) {
      var product = inventoryController.getAllProducts().getBody().stream()
              .filter(it -> it.getId().equals(productId))
              .findFirst()
              .orElseThrow(() -> new RuntimeException("Product not found"));

      if (product.getStock() < quantity) {
          return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Insufficient stock for product: " + productId);
      }
      return ResponseEntity.status(HttpStatus.OK).body(orderService.placeOrder(productId, quantity));
  }
}
