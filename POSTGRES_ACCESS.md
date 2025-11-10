# Guía de Acceso a PostgreSQL

## 📊 Información de Conexión

### PostgreSQL Container
- **Host**: localhost
- **Puerto**: 5432
- **Imagen**: postgres:16

---

## 🔐 Credenciales de Acceso

### Usuario Administrador
- **Usuario**: `tpi_admin`
- **Contraseña**: `SuperSegura_!2025`
- **Base de datos**: `postgres` (default)
- **Privilegios**: Acceso completo a todas las bases de datos

### Usuarios por Servicio

#### Orders Service
- **Base de datos**: `orders_service`
- **Usuario**: `orders_user`
- **Contraseña**: `Orders_!2025`
- **Tablas**: clients, containers, requests, containers_history, solicitud_evento

#### Logistics Service
- **Base de datos**: `logistics_service`
- **Usuario**: `logistics_user`
- **Contraseña**: `Logistics_!2025`
- **Tablas**: deposits, routes, legs, route_deposits

#### Fleet Service
- **Base de datos**: `fleet_service`
- **Usuario**: `fleet_user`
- **Contraseña**: `Fleet_!2025`
- **Tablas**: drivers, trucks, tariffs

---

## 🔧 Métodos de Conexión

### 1. Usando psql (desde la terminal)

#### Conectarse como administrador:
```bash
# Desde dentro del contenedor
docker exec -it postgres-tpi psql -U tpi_admin -d postgres

# Desde tu máquina (si tienes psql instalado)
psql -h localhost -p 5432 -U tpi_admin -d postgres
```

#### Conectarse a una base de datos específica:
```bash
# Orders Service
docker exec -it postgres-tpi psql -U orders_user -d orders_service

# Logistics Service
docker exec -it postgres-tpi psql -U logistics_user -d logistics_service

# Fleet Service
docker exec -it postgres-tpi psql -U fleet_user -d fleet_service
```

### 2. Usando DBeaver

**Configuración de conexión:**
1. Clic en "Nueva Conexión" → PostgreSQL
2. Configurar:
   - **Host**: localhost
   - **Puerto**: 5432
   - **Base de datos**: Elegir una (orders_service, logistics_service, fleet_service, o postgres)
   - **Usuario**: Ver credenciales arriba
   - **Contraseña**: Ver credenciales arriba
3. Test Connection → Finish

### 3. Usando pgAdmin

**Agregar nuevo servidor:**
1. Click derecho en "Servers" → Create → Server
2. **General Tab:**
   - Name: TPI Backend - Orders (o el que corresponda)
3. **Connection Tab:**
   - Host: localhost
   - Port: 5432
   - Maintenance database: orders_service (o la que corresponda)
   - Username: orders_user (o el que corresponda)
   - Password: Orders_!2025 (o la que corresponda)
4. Save

### 4. Usando DataGrip (JetBrains)

**Nueva conexión:**
1. Click en "+" → Data Source → PostgreSQL
2. Configurar:
   - Host: localhost
   - Port: 5432
   - Database: orders_service
   - User: orders_user
   - Password: Orders_!2025
3. Test Connection → Apply

### 5. Usando Azure Data Studio

**Nueva conexión:**
1. New Connection
2. Connection type: PostgreSQL
3. Server: localhost
4. Port: 5432
5. Database: orders_service
6. Username: orders_user
7. Password: Orders_!2025
8. Connect

---

## 📝 Comandos Útiles en psql

### Listar bases de datos
```sql
\l
```

### Conectarse a otra base de datos
```sql
\c orders_service
```

### Listar tablas
```sql
\dt
```

### Describir una tabla
```sql
\d clients
\d+ clients  -- Con más detalles
```

### Ver esquema de una tabla
```sql
SELECT column_name, data_type, character_maximum_length, is_nullable
FROM information_schema.columns
WHERE table_name = 'clients';
```

### Listar usuarios
```sql
\du
```

### Ver conexiones activas
```sql
SELECT * FROM pg_stat_activity;
```

### Salir de psql
```sql
\q
```

---

## 🔍 Consultas de Verificación

### Ver todas las tablas en orders_service
```bash
docker exec -it postgres-tpi psql -U tpi_admin -d orders_service -c "\dt"
```

### Ver datos en la tabla clients
```bash
docker exec -it postgres-tpi psql -U tpi_admin -d orders_service -c "SELECT * FROM clients LIMIT 5;"
```

### Verificar permisos de un usuario
```bash
docker exec -it postgres-tpi psql -U tpi_admin -d postgres -c "\du orders_user"
```

---

## 🛠️ Mantenimiento

### Backup de una base de datos
```bash
# Backup completo de orders_service
docker exec postgres-tpi pg_dump -U orders_user orders_service > orders_backup_$(date +%Y%m%d).sql

# Backup como admin
docker exec postgres-tpi pg_dump -U tpi_admin orders_service > orders_backup_$(date +%Y%m%d).sql
```

### Restaurar desde backup
```bash
# Restaurar orders_service
docker exec -i postgres-tpi psql -U orders_user orders_service < orders_backup_20251108.sql

# O como admin
docker exec -i postgres-tpi psql -U tpi_admin orders_service < orders_backup_20251108.sql
```

### Limpiar una base de datos (CUIDADO!)
```bash
# Eliminar todas las tablas de orders_service
docker exec -it postgres-tpi psql -U tpi_admin -d orders_service -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
```

---

## 🧪 Probar la Conexión

### Verificación Rápida
```bash
# Windows
docker exec -it postgres-tpi psql -U tpi_admin -d postgres -c "SELECT version();"

# Ver todas las bases de datos
docker exec -it postgres-tpi psql -U tpi_admin -c "\l"

# Contar tablas en orders_service
docker exec -it postgres-tpi psql -U tpi_admin -d orders_service -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';"
```

### Script de Verificación Completa
```bash
echo "=== Verificando PostgreSQL ==="
docker exec postgres-tpi psql -U tpi_admin -d postgres -c "SELECT version();"
echo ""
echo "=== Bases de datos ==="
docker exec postgres-tpi psql -U tpi_admin -c "\l"
echo ""
echo "=== Tablas en orders_service ==="
docker exec postgres-tpi psql -U tpi_admin -d orders_service -c "\dt"
echo ""
echo "=== Tablas en logistics_service ==="
docker exec postgres-tpi psql -U tpi_admin -d logistics_service -c "\dt"
echo ""
echo "=== Tablas en fleet_service ==="
docker exec postgres-tpi psql -U tpi_admin -d fleet_service -c "\dt"
```

---

## ⚠️ Notas de Seguridad

1. **Solo para desarrollo**: Estas credenciales son para desarrollo local únicamente
2. **No commitear en producción**: Nunca uses estas contraseñas en producción
3. **Variables de entorno**: En producción, usa variables de entorno o secrets management
4. **Cambiar contraseñas**: Para producción, genera contraseñas fuertes únicas
5. **Firewall**: En producción, restringe el acceso al puerto 5432

---

## 🔄 Reiniciar desde Cero

Si necesitas limpiar todo y empezar de nuevo:

```bash
# Detener y eliminar todo (incluyendo datos)
docker compose down -v

# Iniciar de nuevo
docker compose up -d

# Esperar a que PostgreSQL esté listo
timeout /t 15 /nobreak

# Verificar
health-check.bat
```

---

## 📚 Recursos Adicionales

- [Documentación oficial de PostgreSQL](https://www.postgresql.org/docs/16/)
- [Tutorial de psql](https://www.postgresql.org/docs/16/app-psql.html)
- [Guía de pg_dump](https://www.postgresql.org/docs/16/app-pgdump.html)
