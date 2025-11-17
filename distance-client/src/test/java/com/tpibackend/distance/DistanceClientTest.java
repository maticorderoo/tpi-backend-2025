package com.tpibackend.distance;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tpibackend.distance.mapper.DistanceResponseMapper;
import com.tpibackend.distance.model.DirectionsApiResponse;
import com.tpibackend.distance.model.DistanceResult;

/**
 * Tests para DistanceClient y DistanceResponseMapper.
 * Incluye test unitario con JSON mock y test de integración real (deshabilitado).
 */
@SpringBootTest(classes = TestApp.class)
class DistanceClientTest {

    @Autowired
    private DistanceClient distanceClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Test unitario que deserializa JSON mock y verifica el mapper.
     */
    @Test
    void testDistanceResponseMapperWithMockJson() throws IOException {
        // Leer el JSON de ejemplo
        String json = Files.readString(
            Paths.get("src/test/resources/directions-sample.json")
        );
        
        // Deserializar a DirectionsApiResponse
        DirectionsApiResponse response = objectMapper.readValue(json, DirectionsApiResponse.class);
        
        assertNotNull(response, "La respuesta no debe ser null");
        assertNotNull(response.routes, "Debe tener rutas");
        assertFalse(response.routes.isEmpty(), "Debe haber al menos una ruta");
        
        // Usar el mapper
        DistanceResult data = DistanceResponseMapper.from(response);
        
        assertNotNull(data, "DistanceData no debe ser null");
        
        // Verificar que la suma de legs es correcta
        // Leg 1: 250 km (250000 m), 11700 s
        // Leg 2: 320 km (320000 m), 15000 s
        // Total: 570 km, 26700 s = 445 min
        assertEquals(570.0, data.distanceKm(), 0.01, "Distancia total debe ser 570 km");
        assertEquals(445.0, data.durationMinutes(), 0.01, "Duración total debe ser 445 min");
        
        assertTrue(data.distanceKm() > 0, "La distancia debe ser mayor a 0");
        assertTrue(data.durationMinutes() > 0, "La duración debe ser mayor a 0");
    }

    /**
     * Test que verifica error cuando no hay rutas válidas.
     */
    @Test
    void testMapperThrowsExceptionWhenNoRoutes() {
        DirectionsApiResponse emptyResponse = new DirectionsApiResponse();
        
        assertThrows(IllegalArgumentException.class, () -> {
            DistanceResponseMapper.from(emptyResponse);
        }, "Debe lanzar excepción cuando no hay rutas");
    }

    /**
     * Test de integración real con Google Directions API.
     * Deshabilitado por defecto para no consumir cuota en cada build.
     * Habilitar solo para validar la conexión con Google.
     */
    @Test
    @Disabled("Test real con Google API - habilitar solo para verificación manual")
    void testRealGoogleDirectionsApiCall() {
        // Córdoba a Mendoza
        double cordobaLat = -31.4167;
        double cordobaLng = -64.1833;
        double mendozaLat = -32.8908;
        double mendozaLng = -68.8272;
        
        DistanceResult data = distanceClient.getDistanceAndDuration(
            cordobaLat, cordobaLng,
            mendozaLat, mendozaLng,
            "driving"
        );
        
        assertNotNull(data, "La respuesta no debe ser null");
        assertTrue(data.distanceKm() > 0, "La distancia debe ser mayor a 0");
        assertTrue(data.durationMinutes() > 0, "La duración debe ser mayor a 0");
        
        // Validar que los valores son razonables (Córdoba-Mendoza ~600km)
        assertTrue(data.distanceKm() > 500, "Distancia debe ser > 500 km");
        assertTrue(data.distanceKm() < 800, "Distancia debe ser < 800 km");
        
        System.out.printf("Distancia: %.2f km, Duración: %.2f min%n", 
            data.distanceKm(), data.durationMinutes());
    }

    /**
     * Test con método sobrecargado (sin parámetro mode).
     */
    @Test
    @Disabled("Test real con Google API - habilitar solo para verificación manual")
    void testDefaultModeParameter() {
        double cordobaLat = -31.4167;
        double cordobaLng = -64.1833;
        double buenosAiresLat = -34.6037;
        double buenosAiresLng = -58.3816;
        
        // Sin especificar mode (debería usar "driving" por defecto)
        DistanceResult data = distanceClient.getDistanceAndDuration(
            cordobaLat, cordobaLng,
            buenosAiresLat, buenosAiresLng
        );
        
        assertNotNull(data);
        assertTrue(data.distanceKm() > 0);
        assertTrue(data.durationMinutes() > 0);
    }
}
