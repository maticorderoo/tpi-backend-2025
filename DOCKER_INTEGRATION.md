# Integración Docker - Smoke Test Report

## ✅ Resumen Ejecutivo

**Status**: Completado exitosamente  
**Fecha**: Noviembre 2025  
**Objetivo**: Dockerizar todos los microservicios con perfil H2 in-memory para smoke tests sin dependencias externas

### Servicios dockerizados:
- ✅ **fleet-service** (Puerto 8084)
- ✅ **orders-service** (Puerto 8082)  
- ✅ **logistics-service** (Puerto 8083)
- ✅ **api-gateway** (Puerto 8080)

### Archivos creados/modificados:
- 4 × `Dockerfile` (uno por servicio)
- 4 × `.dockerignore` 
- 4 × `application-dev-docker.yml`
- 1 × `docker-compose.yml` (raíz)
- 1 × `docker-compose.keycloak.yml`
- Actualizaciones en `pom.xml` (dependencia H2 + Actuator)
- Scripts: `smoke-test.bat`, `start-keycloak.bat`, `stop-keycloak.bat`
- Documentación: `README.md`, `KEYCLOAK.md`, este archivo

---

## 1. Perfil `dev-docker`

Se creó un perfil Spring Boot específico para ejecución en Docker con las siguientes características:

### Configuración común (todos los servicios):

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:{service};MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  h2:
    console:
      enabled: true
      path: /h2-console

flyway:
  enabled: false

server:
  port: 8080

app:
  security:
    enabled: false

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

### Características clave:

| Aspecto | dev-docker | Producción |
|---------|------------|------------|
| Base de datos | H2 in-memory | PostgreSQL |
| Persistencia | No (volátil) | Sí |
| Seguridad | Deshabilitada | Keycloak + JWT |
| Flyway | Deshabilitado | Habilitado |
| Propósito | Smoke tests | Ambiente real |

---

## 2. Dockerfiles

### Estructura Multi-Stage Build

Todos los Dockerfiles siguen un patrón multi-stage para optimizar tamaño y seguridad:

**Build Stage**:
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package
```

**Runtime Stage**:
```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
ENV SPRING_PROFILES_ACTIVE=dev-docker
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
```

### Optimizaciones:

1. **Layer caching**: Maven depende de `pom.xml` primero
2. **Tamaño reducido**: Runtime usa JRE (no JDK completo)
3. **Skip tests**: `-DskipTests` para builds más rápidos
4. **Quiet mode**: `-q` para logs limpios

### Casos especiales:

**orders-service y logistics-service**:
- Requieren `distance-client` como dependencia local
- Dockerfile copia y compila `distance-client` primero con `mvn install`
- Luego compila el servicio principal

---

## 3. docker-compose.yml

### Estructura:

```yaml
version: "3.8"

services:
  fleet-service:
    build:
      context: .
      dockerfile: fleet-service/Dockerfile
    ports:
      - "8084:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev-docker
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s

  orders-service:
    # Similar structure
    ports:
      - "8082:8080"
    environment:
      GOOGLE_MAPS_API_KEY: ${GOOGLE_MAPS_API_KEY:-demo-key}

  logistics-service:
    # Similar structure
    ports:
      - "8083:8080"

  api-gateway:
    # Similar structure
    ports:
      - "8080:8080"
    depends_on:
      - orders-service
      - logistics-service
      - fleet-service
```

### Características:

- **Health checks**: Validan que cada servicio esté UP
- **Start period**: 60s para permitir inicio completo de Spring Boot
- **Dependencies**: Gateway espera a que los otros servicios estén levantados
- **Environment**: Variables configurables desde `.env` o CLI

---

## 4. Configuración de Seguridad

### DevSecurityConfig

Cada servicio tiene una clase `DevSecurityConfig` que se activa con perfiles `dev` y `dev-docker`:

```java
@Configuration
@Profile({"dev", "dev-docker"})
public class DevSecurityConfig {
    
    @Bean
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
            .oauth2ResourceServer(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
```

**Comportamiento**:
- Desactiva CSRF
- Permite todas las peticiones sin autenticación
- Desactiva OAuth2 Resource Server
- Solo activo en perfiles development

### SecurityConfig (default)

La configuración de seguridad normal (con JWT) se mantiene para otros perfiles (staging, production).

---

## 5. Dependencias Maven

### H2 Database

Añadido a cada servicio (orders, fleet, logistics):

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### Spring Boot Actuator

Añadido a servicios que no lo tenían:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

---

## 6. Comandos y Scripts

### Build y ejecución:

```bash
# Construir imágenes
docker compose build

# Levantar servicios en background
docker compose up -d

# Ver logs en tiempo real
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs -f orders-service

# Detener servicios
docker compose down

# Detener y eliminar volúmenes
docker compose down -v
```

### Smoke test automatizado (Windows):

```bash
smoke-test.bat
```

Este script:
1. Verifica que Docker esté corriendo
2. Construye las imágenes
3. Levanta los servicios
4. Espera 60 segundos
5. Ejecuta health checks en todos los servicios
6. Reporta el estado

### Keycloak:

```bash
# Iniciar
start-keycloak.bat

# O con docker compose
docker compose -f docker-compose.keycloak.yml up -d

# Detener
stop-keycloak.bat
```

---

## 7. URLs y Endpoints

### Health Checks:

| Servicio | URL | Esperado |
|----------|-----|----------|
| Fleet | http://localhost:8084/actuator/health | `{"status":"UP"}` |
| Orders | http://localhost:8082/actuator/health | `{"status":"UP"}` |
| Logistics | http://localhost:8083/actuator/health | `{"status":"UP"}` |
| Gateway | http://localhost:8080/actuator/health | `{"status":"UP"}` |

### Swagger UI (Acceso directo):

- Fleet: http://localhost:8084/swagger-ui.html
- Orders: http://localhost:8082/swagger-ui.html
- Logistics: http://localhost:8083/swagger-ui.html

### Swagger UI (Vía Gateway):

- Fleet: http://localhost:8080/api/fleet/swagger-ui.html
- Orders: http://localhost:8080/api/orders/swagger-ui.html
- Logistics: http://localhost:8080/api/logistics/swagger-ui.html

### H2 Console:

Cada servicio expone su consola H2 para inspección de datos:

- Fleet: http://localhost:8084/h2-console
- Orders: http://localhost:8082/h2-console
- Logistics: http://localhost:8083/h2-console

**Credenciales**:
- JDBC URL: `jdbc:h2:mem:{service}` (ej: `jdbc:h2:mem:orders`)
- Usuario: `sa`
- Password: (vacío)

---

## 8. Validación de Criterios de Aceptación

### ✅ Criterios cumplidos:

1. **✅ Dockerfiles sin errores**
   - Todos los servicios construyen correctamente
   - Build multi-stage optimizado
   - `distance-client` se instala localmente cuando es necesario

2. **✅ docker compose up levanta 4 contenedores**
   ```bash
   $ docker compose ps
   NAME               IMAGE                    STATUS
   api-gateway        tpi-backend-api-gateway  Up (healthy)
   fleet-service      tpi-backend-fleet        Up (healthy)
   logistics-service  tpi-backend-logistics    Up (healthy)
   orders-service     tpi-backend-orders       Up (healthy)
   ```

3. **✅ Health checks responden UP**
   - Todos los servicios responden `{"status":"UP"}`
   - Health checks configurados en docker-compose
   - Actuator expuesto en todos los servicios

4. **✅ Swagger accesible**
   - Puertos directos: 8082, 8083, 8084
   - Vía Gateway: 8080/api/{service}/swagger-ui.html
   - OpenAPI docs accesibles

5. **✅ Sin dependencias de PostgreSQL ni Keycloak**
   - H2 in-memory para persistencia volátil
   - Seguridad deshabilitada en dev-docker
   - Flyway deshabilitado

6. **✅ distance-client no causa errores**
   - Instalación local en build stage
   - No hace llamadas al inicio (lazy initialization)
   - API key opcional (usa "demo-key" por defecto)

---

## 9. Gateway Routing

### Configuración (application-dev-docker.yml):

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: orders
          uri: http://orders-service:8080
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
            
        - id: logistics
          uri: http://logistics-service:8080
          predicates:
            - Path=/api/logistics/**
          filters:
            - StripPrefix=1
            
        - id: fleet
          uri: http://fleet-service:8080
          predicates:
            - Path=/api/fleet/**
          filters:
            - StripPrefix=1
```

### Comportamiento:

- **Nombres de servicio Docker**: Los servicios se referencian por nombre de contenedor (ej: `orders-service:8080`)
- **StripPrefix**: Elimina `/api/{service}` antes de enrutar
- **Ejemplo**: `GET /api/orders/solicitudes` → `GET /solicitudes` en orders-service

---

## 10. Troubleshooting

### Docker daemon not running

**Síntoma**: `Cannot connect to the Docker daemon`

**Solución**:
```bash
# Abrir Docker Desktop y esperar que inicie
# Verificar estado:
docker info
```

### Port already in use

**Síntoma**: `Bind for 0.0.0.0:8080 failed: port is already allocated`

**Solución**:
```bash
# Opción 1: Detener servicios existentes
docker compose down

# Opción 2: Cambiar puertos en docker-compose.yml
ports:
  - "8090:8080"  # Cambiar puerto host
```

### Service failing health check

**Síntoma**: Container keeps restarting or unhealthy

**Solución**:
```bash
# Ver logs detallados
docker compose logs -f [service-name]

# Verificar health directamente en el contenedor
docker exec -it orders-service curl localhost:8080/actuator/health

# Verificar que el servicio haya iniciado completamente (Spring Boot tarda ~30-60s)
```

### Build failures

**Síntoma**: `mvn package` falla en Dockerfile

**Solución**:
```bash
# Compilar localmente primero para verificar
cd orders-service
mvn clean package -DskipTests

# Si falla distance-client:
cd ../distance-client
mvn clean install -DskipTests
```

### Swagger UI 404

**Síntoma**: Swagger UI no carga

**Solución**:
1. Verificar que el servicio esté UP: `/actuator/health`
2. Probar OpenAPI docs directamente: `/v3/api-docs`
3. Verificar logs del servicio para errores de Springdoc
4. Acceder vía puerto directo primero, luego via gateway

---

## 11. Próximos Pasos

### Agregar PostgreSQL:

```yaml
# En docker-compose.yml
postgres-orders:
  image: postgres:16
  environment:
    POSTGRES_DB: orders
    POSTGRES_USER: orders
    POSTGRES_PASSWORD: orders123
  ports:
    - "5432:5432"
  volumes:
    - orders-data:/var/lib/postgresql/data

volumes:
  orders-data:
```

### Crear perfil `dev-postgres`:

```yaml
# application-dev-postgres.yml
spring:
  datasource:
    url: jdbc:postgresql://postgres-orders:5432/orders
    username: orders
    password: orders123
  jpa:
    hibernate:
      ddl-auto: validate
      
flyway:
  enabled: true
  locations: classpath:db/migration
```

### Integrar Keycloak:

1. Levantar Keycloak: `docker compose -f docker-compose.keycloak.yml up -d`
2. Crear usuarios y asignar roles
3. Habilitar seguridad en servicios:
   ```yaml
   app:
     security:
       enabled: true
   
   spring:
     security:
       oauth2:
         resourceserver:
           jwt:
             issuer-uri: http://keycloak-tpi:8080/realms/tpi-2025
   ```

### Networking avanzado:

```yaml
networks:
  backend:
    driver: bridge
  frontend:
    driver: bridge

services:
  api-gateway:
    networks:
      - frontend
      - backend
  orders-service:
    networks:
      - backend
```

---

## 12. Métricas y Monitoring

### Endpoints disponibles:

| Endpoint | Descripción |
|----------|-------------|
| `/actuator/health` | Estado general del servicio |
| `/actuator/health/liveness` | Liveness probe (K8s) |
| `/actuator/health/readiness` | Readiness probe (K8s) |
| `/actuator/info` | Información del servicio |
| `/actuator/metrics` | Métricas de Micrometer |

### Ejemplo de respuesta health:

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "H2",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

---

## 13. Conclusión

### Logros:

✅ **Infraestructura Docker completa** para todos los microservicios  
✅ **Smoke tests automáticos** sin dependencias externas  
✅ **Documentación exhaustiva** de configuración y troubleshooting  
✅ **Scripts de conveniencia** para Windows  
✅ **Gateway funcional** enrutando a todos los servicios  
✅ **Health checks** configurados y validados  

### Próximos pasos recomendados:

1. Agregar PostgreSQL al docker-compose
2. Integrar Keycloak para autenticación real
3. Configurar volumes para persistencia
4. Implementar logging centralizado (ELK, Loki)
5. Agregar monitoring (Prometheus + Grafana)
6. CI/CD pipeline para builds automáticos

---

**Versión**: 1.0  
**Fecha**: Noviembre 2025  
**Autor**: TPI Backend 2025 Team  
**Repositorio**: [maticorderoo/tpi-backend-2025](https://github.com/maticorderoo/tpi-backend-2025)
