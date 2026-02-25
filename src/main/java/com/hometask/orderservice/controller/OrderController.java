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

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

  @PostMapping("/place/{productId}/{quantity}")
  public ResponseEntity<String> placeOrder(@PathVariable String productId, @PathVariable int quantity) {
      return ResponseEntity.status(HttpStatus.OK).body(orderService.placeOrder(productId, quantity));
  }
}
