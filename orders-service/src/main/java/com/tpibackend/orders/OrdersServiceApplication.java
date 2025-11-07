package com.tpibackend.orders;

import com.tpibackend.orders.config.FleetClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = {"com.tpibackend.orders", "com.tpibackend.distance"})
@EnableConfigurationProperties(FleetClientProperties.class)
public class OrdersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersServiceApplication.class, args);
    }
}
