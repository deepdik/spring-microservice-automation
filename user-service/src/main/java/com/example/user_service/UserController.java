package com.example.user_service;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.reactive.function.client.WebClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping("/order")
    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackOrder")
    public String getUserWithOrder() {
        return webClientBuilder.build()
                .get()
                .uri("http://order-service/order")
                .retrieve()
                .bodyToMono(String.class)
                .block();  // Blocking to keep the code synchronous like RestTemplate
    }

    // Simple URL endpoint
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from User Service!";
    }

    // Fallback methods
    public String fallbackOrder(Throwable t) {
        return "Order service is temporarily unavailable.";
    }

}
