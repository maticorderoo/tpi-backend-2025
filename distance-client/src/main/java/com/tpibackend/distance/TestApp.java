package com.tpibackend.distance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.tpibackend.distance.model.DistanceResult;

/**
 * Aplicación de prueba para validar el DistanceClient.
 * Ejecutar con: mvn spring-boot:run
 */
@SpringBootApplication
public class TestApp {

    public static void main(String[] args) {
        SpringApplication.run(TestApp.class, args);
    }

    @Bean
    CommandLineRunner demo(DistanceClient distanceClient) {
        return args -> {
            System.out.println("\n=== Distance Client Demo ===\n");
            
            try {
                // Córdoba a Mendoza
                DistanceResult data = distanceClient.getDistanceAndDuration(
                    -31.4167, -64.1833,   // Córdoba Capital
                    -32.8908, -68.8272,   // Mendoza
                    "driving"
                );
                
                System.out.printf("🚗 Córdoba → Mendoza%n");
                System.out.printf("   Distancia: %.2f km%n", data.distanceKm());
                System.out.printf("   Duración: %.2f min (%.2f hs)%n%n", 
                    data.durationMinutes(), data.durationMinutes() / 60.0);
                
            } catch (Exception e) {
                System.err.println("❌ Error: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }
}
