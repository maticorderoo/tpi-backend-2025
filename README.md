# TPI Backend 2025

Este repositorio contiene los microservicios Spring Boot que conforman la solución del TPI Backend 2025:

- **orders-service** (`com.tpibackend.orders`)
- **logistics-service** (`com.tpibackend.logistics`)
- **fleet-service** (`com.tpibackend.fleet`)
- **api-gateway** (`com.tpibackend.gateway`)
- **distance-client** (`com.tpibackend.distance`)
- **keycloak** (realm `tpi-2025`)

## Requisitos

- Java 21
- Maven 3.9+
- **Docker Desktop** (para ejecución en contenedores)
- Keycloak 22+ (para ejecutar el realm `keycloak/realm-export/tpi-2025-realm.json`)

## Ejecución con Docker 🐳 (Recomendado para smoke test)

### Inicio rápido

```bash
# Construir imágenes
docker compose build

# Levantar todos los servicios
docker compose up -d

# Ver logs
docker compose logs -f

# Health checks
curl http://localhost:8084/actuator/health  # Fleet
curl http://localhost:8082/actuator/health  # Orders
curl http://localhost:8083/actuator/health  # Logistics
curl http://localhost:8080/actuator/health  # Gateway

# Detener servicios
docker compose down
```

### URLs con Docker

| Servicio | Health | Swagger UI | Puerto |
|----------|--------|------------|--------|
| Fleet | http://localhost:8084/actuator/health | http://localhost:8084/swagger-ui.html | 8084 |
| Orders | http://localhost:8082/actuator/health | http://localhost:8082/swagger-ui.html | 8082 |
| Logistics | http://localhost:8083/actuator/health | http://localhost:8083/swagger-ui.html | 8083 |
| Gateway | http://localhost:8080/actuator/health | N/A | 8080 |

**Vía Gateway**:
- http://localhost:8080/api/fleet/swagger-ui.html
- http://localhost:8080/api/orders/swagger-ui.html
- http://localhost:8080/api/logistics/swagger-ui.html

### Perfil `dev-docker`

Los servicios en Docker usan el perfil `dev-docker` que:
- ✅ Usa H2 in-memory (sin persistencia)
- ✅ No requiere PostgreSQL
- ✅ No requiere Keycloak (seguridad deshabilitada)
- ✅ Ideal para smoke tests y validación rápida

**Nota**: Los datos se pierden al detener los contenedores. Para persistencia, usa el perfil con PostgreSQL.

### Documentación completa

Ver [INTEGRATION_REPORT.md](INTEGRATION_REPORT.md) para:
- Arquitectura Docker detallada
- Troubleshooting
- Diferencias entre perfiles
- Próximos pasos (agregar PostgreSQL/Keycloak)

## Requisitos

- Java 21
- Maven 3.9+
- Keycloak 22+ (para ejecutar el realm `keycloak/realm-export/tpi-2025-realm.json`)

## Build rápido

```bash
mvn -q -DskipTests package
```

## Ejecución local

1. Levantar Keycloak con el realm `tpi-2025` (puerto 8080 por defecto).
2. Ejecutar los servicios en el siguiente orden para respetar dependencias:
   1. **fleet-service**
   2. **orders-service**
   3. **logistics-service**
   4. **api-gateway**

Cada módulo expone scripts de conveniencia:

```bash
./fleet-service/run-dev            # Ejecuta con seguridad habilitada
./fleet-service/run-dev-noauth     # Perfil dev sin seguridad
./orders-service/run-dev
./orders-service/run-dev-noauth
./logistics-service/run-dev
./logistics-service/run-dev-noauth
./api-gateway/run-dev
./api-gateway/run-dev-noauth
```

Las URLs de Swagger/OpenAPI son:

- Fleet: <http://localhost:8084/swagger-ui.html>
- Orders: <http://localhost:8082/swagger-ui.html>
- Logistics: <http://localhost:8083/swagger-ui.html>
- Gateway (proxy de documentaciones):
  - <http://localhost:8080/api/fleet/swagger-ui.html>
  - <http://localhost:8080/api/orders/swagger-ui.html>
  - <http://localhost:8080/api/logistics/swagger-ui.html>

## Colecciones de pruebas

En la raíz encontrarás:

- `TPI-2025.postman_collection.json`: requests del flujo end-to-end.
- `TPI-2025.local-dev.postman_environment.json`: variables para golpear servicios directamente.
- `TPI-2025.gateway-dev.postman_environment.json`: variables para consumir a través del API Gateway.

El flujo recomendado:

1. Crear una solicitud en Orders.
2. Consultar la solicitud y obtener IDs generados.
3. Crear un camión y consultar métricas en Fleet.
4. Generar ruta y asignarla a la solicitud en Logistics.
5. Asignar camión, iniciar y finalizar tramos.
6. Volver a Orders para recalcular estimaciones y seguimiento.

Para entornos con seguridad, asigná tokens Keycloak con roles `CLIENTE`, `OPERADOR` y `TRANSPORTISTA` según corresponda. Para pruebas sin autenticación, ejecutá los scripts `run-dev-noauth` y omite el header `Authorization`.

## Healthchecks

Cada micro expone `/actuator/health` o `/health` según corresponda; utilízalos para smoke tests rápidos.

## Keycloak 🔐

### Inicio rápido

```bash
# Opción 1: Script batch (Windows)
start-keycloak.bat

# Opción 2: Docker Compose
docker compose -f docker-compose.keycloak.yml up -d

# Ver logs
docker logs -f keycloak-tpi

# Detener
docker stop keycloak-tpi
# O ejecutar: stop-keycloak.bat
```

### Acceso
- **URL**: http://localhost:8080
- **Admin Console**: http://localhost:8080/admin
- **Usuario**: `admin`
- **Contraseña**: `admin`
- **Realm**: `tpi-2025`
- **Roles**: `CLIENTE`, `OPERADOR`, `TRANSPORTISTA`

Ver [KEYCLOAK.md](KEYCLOAK.md) para más detalles sobre:
- Crear usuarios de prueba
- Obtener tokens JWT
- Integración con servicios
- Troubleshooting

---

**Repositorio**: [maticorderoo/tpi-backend-2025](https://github.com/maticorderoo/tpi-backend-2025)

