package com.tpibackend.orders;

import com.tpibackend.orders.config.DistanceClientProperties;
import com.tpibackend.orders.config.FleetClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({DistanceClientProperties.class, FleetClientProperties.class})
public class OrdersServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrdersServiceApplication.class, args);
    }
}
