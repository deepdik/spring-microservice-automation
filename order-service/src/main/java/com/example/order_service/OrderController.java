package com.example.order_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders") // or /orders
public class OrderController {
    @GetMapping
    public String getUsers() {
        return "Hello from order-service!";
    }
}
