package com.example.user_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private WebClient.Builder webClientBuilder;  // Auto-configured, tracing-aware

    @Autowired
    private ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory;

    @GetMapping("/order")
    public Mono<String> getUserWithOrder() {
        return circuitBreakerFactory.create("orderService")
                .run(
                        webClientBuilder.build()
                                .get()
                                .uri("http://order-service/order")
                                .retrieve()
                                .bodyToMono(String.class),
                        throwable -> Mono.just("Fallback: Order service is unavailable")
                );
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from User Service!";
    }
}
