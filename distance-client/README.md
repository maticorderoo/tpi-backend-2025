# 🌍 Distance Client

Cliente reutilizable para calcular distancias y tiempos de viaje usando **Google Maps Directions API**.

Diseñado para el TPI Backend (sistema de logística) de la UTN, permite calcular rutas entre coordenadas con soporte para múltiples tramos, diferentes modos de transporte, manejo robusto de errores y logging.

---

## ✨ Características

- ✅ **Cálculo de distancia y tiempo** entre dos coordenadas geográficas
- ✅ **Soporte para múltiples tramos** (legs) - suma automática de todos los segmentos de la ruta
- ✅ **Modos de transporte configurables**: `driving`, `walking`, `bicycling`, `transit`
- ✅ **Manejo de errores robusto** con mensajes claros (4xx, 5xx, API Key inválida)
- ✅ **Timeouts configurados**: 10s de respuesta, 5s de conexión
- ✅ **Logging completo** con SLF4J/Lombok (@Slf4j)
- ✅ **Tests unitarios y de integración** incluidos

---

## 📦 Instalación

### Maven

Agrega esta dependencia en tu `pom.xml`:

```xml
<dependency>
    <groupId>com.tpibackend.distance</groupId>
    <artifactId>distance-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```gradle
implementation 'com.tpibackend.distance:distance-client:1.0.0'
```

---

## ⚙️ Configuración

### 1. API Key de Google Maps

Necesitas una API Key de Google Cloud con la **Directions API** habilitada.

Configura la key en tu `application.properties` o `application.yml`:

**application.properties:**
```properties
google.maps.api.key=TU_API_KEY_AQUI
```

**application.yml:**
```yaml
google:
  maps:
    api:
      key: TU_API_KEY_AQUI
```

⚠️ **IMPORTANTE:** Para uso académico. **NO subas tu API Key a repositorios públicos**. Usa variables de entorno o archivos `.env` en producción.

### 2. Obtener una API Key gratuita

1. Ve a [Google Cloud Console](https://console.cloud.google.com/)
2. Crea un proyecto nuevo
3. Habilita **Directions API**
4. Crea credenciales → API Key
5. (Opcional) Restringe la key por IP o dominio

---

## 🚀 Uso

### Inyección del cliente

```java
import com.tpibackend.distance.DistanceClient;
import org.springframework.stereotype.Service;

@Service
public class LogisticsService {
    
    private final DistanceClient distanceClient;
    
    public LogisticsService(DistanceClient distanceClient) {
        this.distanceClient = distanceClient;
    }
    
    // ... tus métodos
}
```

### Ejemplo básico (modo "driving" por defecto)

```java
// Coordenadas: Córdoba a Mendoza
double cordobaLat = -31.4167;
double cordobaLng = -64.1833;
double mendozaLat = -32.8908;
double mendozaLng = -68.8272;

DistanceData data = distanceClient.getDistance(
    cordobaLat, cordobaLng, 
    mendozaLat, mendozaLng
);

System.out.printf("Distancia: %.2f km%n", data.distanceKm());
System.out.printf("Duración: %.2f minutos%n", data.durationMinutes());
```

**Salida esperada:**
```
Distancia: 610.50 km
Duración: 450.25 minutos
```

### Ejemplo con modo de transporte específico

```java
// Calcular ruta caminando
DistanceData walking = distanceClient.getDistance(
    -31.4200, -64.1880,  // Plaza San Martín, Córdoba
    -31.4100, -64.1820,  // Parque Sarmiento
    "walking"
);

// Calcular ruta en bicicleta
DistanceData cycling = distanceClient.getDistance(
    -34.6037, -58.3816,  // Buenos Aires
    -34.5500, -58.4500,  // Palermo
    "bicycling"
);

// Calcular ruta en transporte público
DistanceData transit = distanceClient.getDistance(
    -31.4167, -64.1833,  // Córdoba
    -31.3200, -64.2100,  // Villa Carlos Paz
    "transit"
);
```

### Modos de transporte soportados

| Modo | Descripción |
|------|-------------|
| `driving` | Automóvil (por defecto) |
| `walking` | Caminando |
| `bicycling` | Bicicleta |
| `transit` | Transporte público |

---

## 🧪 Testing

### Ejecutar tests

```bash
# Ejecutar solo tests unitarios (sin consumir API)
mvn test

# Ejecutar tests con cobertura
mvn clean test jacoco:report
```

### Test con JSON mock (sin consumir cuota de API)

El módulo incluye `src/test/resources/directions-sample.json` con una respuesta de ejemplo que simula una ruta con 2 tramos (Córdoba → Intermedio → Mendoza).

```java
@Test
void testDistanceResponseMapperWithMockJson() throws IOException {
    String json = Files.readString(
        Paths.get("src/test/resources/directions-sample.json")
    );
    
    DirectionsApiResponse response = objectMapper.readValue(
        json, DirectionsApiResponse.class
    );
    
    DistanceData data = DistanceResponseMapper.from(response);
    
    assertEquals(570.0, data.distanceKm(), 0.01);
    assertEquals(445.0, data.durationMinutes(), 0.01);
}
```

### Test real (deshabilitado por defecto)

Para validar la conexión con Google:

```java
@Test
@Disabled("Test real con Google API - habilitar solo para verificación manual")
void testRealGoogleDirectionsApiCall() {
    DistanceData data = distanceClient.getDistance(
        -31.4167, -64.1833,  // Córdoba
        -32.8908, -68.8272,  // Mendoza
        "driving"
    );
    
    assertTrue(data.distanceKm() > 500);
    assertTrue(data.distanceKm() < 800);
}
```

**Para habilitarlo:** Quita la anotación `@Disabled` temporalmente.

---

## 📊 Manejo de Errores

El cliente maneja automáticamente los siguientes escenarios:

### API Key inválida (403)
```
IllegalStateException: API Key inválida o solicitud incorrecta. 
Verifica tu configuración.
```

### Error del servidor de Google (5xx)
```
IllegalStateException: Error del servidor de Google Maps (5xx): ...
```

### Respuesta vacía o sin rutas
```
IllegalStateException: La respuesta no contiene rutas válidas
```

### Timeout de conexión
```
IllegalStateException: Error al conectar con Google Directions API: 
Read timed out
```

Todos los errores se loguean con contexto completo (origin, destination, mode).

---

## 🔧 Arquitectura

```
distance-client/
├── src/main/java/com/tpibackend/distance/
│   ├── DistanceClient.java           # Cliente principal con @Slf4j
│   ├── config/
│   │   └── WebClientConfig.java      # Configuración de timeouts
│   ├── mapper/
│   │   └── DistanceResponseMapper.java  # Convierte API → DistanceData
│   └── model/
│       ├── DirectionsApiResponse.java   # DTO de respuesta de Google
│       └── DistanceData.java            # Record con km y minutos
└── src/test/
    ├── java/
    │   └── DistanceClientTest.java      # Tests unitarios + integración
    └── resources/
        └── directions-sample.json       # JSON mock para tests
```

### Flujo de datos

1. **Cliente llama** `getDistance(lat, lng, lat, lng, mode)`
2. **DistanceClient** hace request HTTP a Google Directions API (con timeouts)
3. **WebClient** deserializa JSON a `DirectionsApiResponse`
4. **DistanceResponseMapper** suma todos los `legs` y crea `DistanceData`
5. **Se loguea** el resultado y se devuelve al cliente

---

## 🔍 Logging

Todos los logs usan SLF4J con Lombok `@Slf4j`:

**Ejemplo de logs:**
```
INFO  - Solicitando ruta: origin=-31.4167,-64.1833, destination=-32.8908,-68.8272, mode=driving
INFO  - Ruta calculada exitosamente: 610.50 km, 450.25 min
```

**En caso de error:**
```
ERROR - Error 4xx en Google Directions API: {"status":"REQUEST_DENIED"}
ERROR - Error HTTP al llamar a Google Directions API: status=403, body=...
```

---

## 📝 Notas Adicionales

### Límites de la API de Google

- **Modo gratuito:** 40,000 requests/mes
- **Costo por request adicional:** ~$5 USD por 1000 requests
- Ver [precios oficiales](https://mapsplatform.google.com/pricing/)

### Optimización de costos

- Cachea resultados cuando sea posible
- Usa el modo `walking` o `bicycling` solo cuando sea necesario
- Agrupa requests batch si es posible

### Uso académico

Este módulo está diseñado para fines educativos en la UTN. Asegúrate de:

- ✅ No compartir tu API Key
- ✅ Usar restricciones de IP/dominio en producción
- ✅ Monitorear el uso en Google Cloud Console

---

## 🤝 Integración con otros módulos

### En `orders-service`

```java
@Service
public class OrderService {
    
    private final DistanceClient distanceClient;
    
    public void calculateDeliveryCost(Order order) {
        DistanceData data = distanceClient.getDistance(
            order.getWarehouseLat(), order.getWarehouseLng(),
            order.getDeliveryLat(), order.getDeliveryLng(),
            "driving"
        );
        
        double cost = data.distanceKm() * 0.5; // $0.50 por km
        order.setDeliveryCost(cost);
    }
}
```

### En `logistics-service`

```java
@Service
public class RouteOptimizerService {
    
    private final DistanceClient distanceClient;
    
    public List<Route> optimizeDeliveries(List<Delivery> deliveries) {
        // Calcular matriz de distancias
        for (Delivery d : deliveries) {
            DistanceData data = distanceClient.getDistance(
                d.getOriginLat(), d.getOriginLng(),
                d.getDestLat(), d.getDestLng(),
                "driving"
            );
            d.setEstimatedTime(data.durationMinutes());
        }
        
        // Aplicar algoritmo de optimización...
        return routes;
    }
}
```

---

## 📚 Referencias

- [Google Directions API Docs](https://developers.google.com/maps/documentation/directions)
- [Spring WebFlux WebClient](https://docs.spring.io/spring-framework/reference/web/webflux-webclient.html)
- [Reactor Netty HTTP Client](https://projectreactor.io/docs/netty/release/reference/)

---

## 📄 Licencia

Proyecto académico - UTN 2025

---

## ✍️ Autor

TPI Backend 2025 - Sistema de Logística
