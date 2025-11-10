# PostgreSQL Integration Guide

## Descripción

Cada microservicio se conecta a su propia base de datos PostgreSQL en un contenedor compartido. Los esquemas y datos están completamente aislados por base de datos.

## Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    PostgreSQL Container                      │
│  ┌──────────────┬──────────────────┬─────────────────┐     │
│  │ orders_      │ logistics_       │ fleet_          │     │
│  │ service      │ service          │ service         │     │
│  │              │                  │                 │     │
│  │ User:        │ User:            │ User:           │     │
│  │ orders_user  │ logistics_user   │ fleet_user      │     │
│  └──────────────┴──────────────────┴─────────────────┘     │
└─────────────────────────────────────────────────────────────┘
         ▲                  ▲                  ▲
         │                  │                  │
    ┌────┴────┐      ┌─────┴──────┐     ┌────┴─────┐
    │ Orders  │      │ Logistics  │     │  Fleet   │
    │ Service │      │  Service   │     │ Service  │
    └─────────┘      └────────────┘     └──────────┘
```

## Bases de Datos

### 1. orders_service
- **Usuario**: `orders_user` / `Orders_!2025`
- **Tablas**: 
  - `clients` - Clientes
  - `containers` - Contenedores
  - `requests` - Solicitudes de transporte

### 2. logistics_service
- **Usuario**: `logistics_user` / `Logistics_!2025`
- **Tablas**:
  - `deposits` - Depósitos
  - `routes` - Rutas
  - `legs` - Tramos de ruta
  - `route_deposits` - Relación rutas-depósitos

### 3. fleet_service
- **Usuario**: `fleet_user` / `Fleet_!2025`
- **Tablas**:
  - `drivers` - Conductores
  - `trucks` - Camiones
  - `tariffs` - Tarifas

### Usuario Admin
- **Usuario**: `tpi_admin` / `SuperSegura_!2025`
- **Privilegios**: Acceso completo a todas las bases de datos

## Inicio Rápido

### 1. Levantar el Sistema

```bash
# Windows
start-postgres.bat

# Linux/Mac
docker compose up -d
```

### 2. Verificar Estado

```bash
# Windows
health-check.bat

# Linux/Mac
docker compose ps
docker compose logs -f
```

### 3. Acceder a las APIs

- **API Gateway**: http://localhost:8081
- **Orders Service**: http://localhost:8082
- **Logistics Service**: http://localhost:8083
- **Fleet Service**: http://localhost:8084

### 4. Swagger UI

- **Orders**: http://localhost:8082/swagger-ui.html
- **Logistics**: http://localhost:8083/swagger-ui.html
- **Fleet**: http://localhost:8084/swagger-ui.html

## Configuración

### Perfil: dev-postgres

Cada servicio tiene un archivo `application-dev-postgres.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://postgres:5432/[database_name]
    username: [service_user]
    password: [service_password]
  jpa:
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    baseline-on-migrate: true
```

### Flyway Migrations

Los esquemas se inicializan automáticamente mediante:
1. Scripts SQL en `/orders-service/initdb/` (para inicialización PostgreSQL)
2. Migraciones Flyway en cada servicio (para evolución del esquema)

## Conexión Directa a PostgreSQL

### Con psql

```bash
# Conectarse a una base específica
docker exec -it postgres-tpi psql -U orders_user -d orders_service

# Como admin
docker exec -it postgres-tpi psql -U tpi_admin -d postgres
```

### Con Cliente GUI (DBeaver, pgAdmin, etc.)

```
Host:     localhost
Port:     5432
Database: orders_service (o logistics_service, fleet_service)
Usuario:  orders_user (o logistics_user, fleet_user, tpi_admin)
Password: (ver credenciales arriba)
```

## Scripts de Inicialización

Los scripts en `/orders-service/initdb/` se ejecutan automáticamente al crear el contenedor PostgreSQL:

1. **01-create-databases-and-roles.sql** - Crea bases de datos y usuarios
2. **02-orders-schema.sql** - Esquema de orders_service
3. **03-logistics-schema.sql** - Esquema de logistics_service
4. **04-fleet-schema.sql** - Esquema de fleet_service

## Comandos Útiles

### Docker Compose

```bash
# Detener todos los servicios
docker compose down

# Reiniciar un servicio específico
docker compose restart orders-service

# Ver logs
docker compose logs -f orders-service

# Reconstruir y reiniciar
docker compose up -d --build
```

### Gestión de Datos

```bash
# Eliminar volumen (CUIDADO: borra todos los datos)
docker compose down -v

# Backup de una base de datos
docker exec postgres-tpi pg_dump -U orders_user orders_service > backup.sql

# Restore
docker exec -i postgres-tpi psql -U orders_user orders_service < backup.sql
```

## Troubleshooting

### Los servicios no arrancan

1. Verificar que PostgreSQL esté healthy:
   ```bash
   docker compose logs postgres
   ```

2. Verificar credenciales en `application-dev-postgres.yml`

3. Revisar logs del servicio:
   ```bash
   docker compose logs [service-name]
   ```

### Error de conexión a la base de datos

- Verificar que el perfil `dev-postgres` esté activo
- Verificar que PostgreSQL esté escuchando en el puerto 5432
- Verificar que las credenciales sean correctas

### Flyway error

Si Flyway falla al validar el esquema:

```bash
# Opción 1: Limpiar y recrear (CUIDADO: borra datos)
docker compose down -v
docker compose up -d

# Opción 2: Baseline manual
docker exec -it postgres-tpi psql -U tpi_admin -d orders_service
# Luego ejecutar comandos Flyway según sea necesario
```

## Seguridad

⚠️ **IMPORTANTE**: Las credenciales mostradas son para desarrollo local únicamente.

Para producción:
- Cambiar todas las contraseñas
- Usar variables de entorno o secrets management
- Habilitar SSL/TLS para conexiones
- Configurar límites de conexiones
- Implementar backup automatizado

## Perfiles Disponibles

- **dev**: Desarrollo local sin Docker (requiere PostgreSQL local)
- **dev-docker**: Desarrollo con H2 en memoria (deprecated)
- **dev-postgres**: Desarrollo con PostgreSQL en Docker ✅ (recomendado)
- **prod**: Producción con OAuth2 y PostgreSQL externo
