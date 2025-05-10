//import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.reactive.function.client.WebClient;
//
//@Configuration
//public class WebClientConfig {
//
//    @Bean
//    public WebClient.Builder webClientBuilder(ReactiveResilience4JCircuitBreakerFactory circuitBreakerFactory) {
//        return WebClient.builder(); // You apply circuit breaker at usage time, not here
//    }
//}
