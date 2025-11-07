# 🐳 Docker Quick Start - TPI Backend 2025

## Inicio Rápido (3 comandos)

```bash
# 1. Construir imágenes
docker compose build

# 2. Levantar servicios  
docker compose up -d

# 3. Verificar (esperar ~60s)
curl http://localhost:8084/actuator/health  # Fleet
curl http://localhost:8082/actuator/health  # Orders
curl http://localhost:8083/actuator/health  # Logistics
curl http://localhost:8080/actuator/health  # Gateway
```

## O usa el script automatizado (Windows)

```bash
smoke-test.bat
```

## URLs Importantes

### Health Checks
- Fleet: http://localhost:8084/actuator/health
- Orders: http://localhost:8082/actuator/health
- Logistics: http://localhost:8083/actuator/health
- Gateway: http://localhost:8080/actuator/health

### Swagger UI
- Fleet: http://localhost:8084/swagger-ui.html
- Orders: http://localhost:8082/swagger-ui.html
- Logistics: http://localhost:8083/swagger-ui.html

### Vía Gateway
- http://localhost:8080/api/fleet/swagger-ui.html
- http://localhost:8080/api/orders/swagger-ui.html
- http://localhost:8080/api/logistics/swagger-ui.html

## Comandos Útiles

```bash
# Ver logs en tiempo real
docker compose logs -f

# Ver logs de un servicio específico
docker compose logs -f orders-service

# Reiniciar un servicio
docker compose restart orders-service

# Detener todos los servicios
docker compose down

# Detener y eliminar todo (incluye volúmenes)
docker compose down -v

# Ver estado de contenedores
docker compose ps

# Reconstruir un servicio específico
docker compose build orders-service
docker compose up -d orders-service
```

## Características del Perfil dev-docker

✅ **H2 in-memory** - No requiere PostgreSQL  
✅ **Sin autenticación** - No requiere Keycloak  
✅ **Flyway deshabilitado** - Esquema creado automáticamente  
✅ **Actuator expuesto** - Health checks y métricas  
✅ **Swagger habilitado** - Documentación API interactiva  

⚠️ **Advertencia**: Los datos no persisten al reiniciar contenedores

## Troubleshooting

### Docker no inicia
```bash
# Asegúrate de que Docker Desktop esté corriendo
docker info
```

### Puerto en uso
```bash
# Cambiar puerto en docker-compose.yml o detener servicio conflictivo
docker compose down
```

### Servicio no responde
```bash
# Ver logs detallados
docker compose logs -f [service-name]

# Verificar desde dentro del contenedor
docker exec -it orders-service curl localhost:8080/actuator/health
```

### Build falla
```bash
# Limpiar todo y reconstruir
docker compose down -v
docker system prune -f
docker compose build --no-cache
```

## Documentación Completa

- **README.md** - Visión general del proyecto
- **DOCKER_INTEGRATION.md** - Guía completa de Docker
- **KEYCLOAK.md** - Configuración de autenticación
- **INTEGRATION_REPORT.md** - Reporte técnico detallado

## Próximos Pasos

1. **Agregar PostgreSQL**: Ver sección "Próximos pasos" en DOCKER_INTEGRATION.md
2. **Habilitar Keycloak**: `start-keycloak.bat` o `docker compose -f docker-compose.keycloak.yml up -d`
3. **Configurar persistencia**: Añadir volumes en docker-compose.yml
4. **Ambiente de staging**: Crear perfil staging con configuración real

---

**¿Problemas?** Ver DOCKER_INTEGRATION.md sección "Troubleshooting" para soluciones detalladas.
