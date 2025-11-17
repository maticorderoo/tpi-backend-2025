package com.tpibackend.logistics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.tpibackend.logistics.config.EstimacionProperties;

@SpringBootApplication(scanBasePackages = {"com.tpibackend.logistics", "com.tpibackend.distance"})
@EnableConfigurationProperties(EstimacionProperties.class)
public class LogisticsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogisticsServiceApplication.class, args);
    }
}
