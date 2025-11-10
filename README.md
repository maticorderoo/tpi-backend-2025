# TPI Backend 2025

Este repositorio contiene los microservicios Spring Boot que conforman la solución del TPI Backend 2025:

- **orders-service** (`com.tpibackend.orders`)
- **logistics-service** (`com.tpibackend.logistics`)
- **fleet-service** (`com.tpibackend.fleet`)
- **api-gateway** (`com.tpibackend.gateway`)
- **distance-client** (`com.tpibackend.distance`)
- **PostgreSQL 16** (3 bases de datos separadas)
- **Keycloak 23** (realm `tpi-2025`)

## Requisitos

- Java 21
- Maven 3.9+
- **Docker Desktop** (para ejecución en contenedores)
- **PostgreSQL 16** (incluido en Docker Compose)

## 🚀 Inicio Rápido con Docker + PostgreSQL

### Levantar el sistema completo

```bash
# Windows
start-postgres.bat

# Linux/Mac
docker compose up -d
```

### Verificar estado

```bash
# Health check de todos los servicios
health-check.bat

# Verificar PostgreSQL
verify-postgres.bat
```

### URLs de Acceso

| Servicio | Health | Swagger UI | Puerto |
|----------|--------|------------|--------|
| API Gateway | http://localhost:8081/actuator/health | N/A | 8081 |
| Orders | http://localhost:8082/actuator/health | http://localhost:8082/swagger-ui.html | 8082 |
| Logistics | http://localhost:8083/actuator/health | http://localhost:8083/swagger-ui.html | 8083 |
| Fleet | http://localhost:8084/actuator/health | http://localhost:8084/swagger-ui.html | 8084 |
| PostgreSQL | localhost:5432 | N/A | 5432 |

### 🗄️ Acceso a PostgreSQL

#### Conexión Rápida
```bash
# Script interactivo para conectarse
connect-postgres.bat

# O directamente:
docker exec -it postgres-tpi psql -U tpi_admin -d postgres
```

#### Credenciales

**Administrador:**
- Usuario: `tpi_admin`
- Contraseña: `SuperSegura_!2025`

**Por servicio:**
- **orders_service**: `orders_user` / `Orders_!2025`
- **logistics_service**: `logistics_user` / `Logistics_!2025`  
- **fleet_service**: `fleet_user` / `Fleet_!2025`

**Ver guía completa**: [POSTGRES_ACCESS.md](POSTGRES_ACCESS.md)

### Perfil `dev-postgres`

Los servicios en Docker usan el perfil `dev-postgres` que:
- ✅ Usa PostgreSQL 16 con bases de datos separadas
- ✅ Persistencia de datos en volúmenes Docker
- ✅ No requiere Keycloak (seguridad deshabilitada)
- ✅ Hibernate en modo `update` (crea/actualiza tablas automáticamente)
- ✅ Ideal para desarrollo con datos persistentes

**Nota**: Para eliminar todos los datos: `docker compose down -v`

### Documentación

- **[POSTGRES_ACCESS.md](POSTGRES_ACCESS.md)** - Guía completa de acceso a PostgreSQL
- **[POSTGRES_INTEGRATION.md](POSTGRES_INTEGRATION.md)** - Arquitectura y configuración
- **[KEYCLOAK.md](KEYCLOAK.md)** - Configuración de autenticación
- **[DOCKER_QUICKSTART.md](DOCKER_QUICKSTART.md)** - Inicio rápido con Docker

## Ejecución Local (sin Docker)

### Requisitos adicionales
- PostgreSQL 16 instalado localmente
- Keycloak 23+ con realm `tpi-2025`

### Setup

1. Crear las bases de datos en PostgreSQL local
2. Ejecutar scripts de inicialización en `orders-service/initdb/`
3. Levantar Keycloak con el realm `tpi-2025` (puerto 8080)
4. Ejecutar servicios con perfil `dev`:

```bash
./fleet-service/run-dev
./orders-service/run-dev
./logistics-service/run-dev
./api-gateway/run-dev
```
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

